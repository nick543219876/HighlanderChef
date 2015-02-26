package com.highlanderchef;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Comm {
	private static String serverRoot = "http://96.126.122.162:9222/chef/";
	private static String serverImgRoot = "http://96.126.122.162:9223/";
	private static boolean runningAndroid = true;
	private static final int commVersion = 1;

	private static ObjectMapper mapper;

	private int lastStatus;
	private String lastJSON;
	private JsonNode rootNode;

	// User account info
	private static volatile User user;
	private static volatile String authToken = "";

	// Image cache
	private static volatile HashMap<String, CacheItem> imagecache;
	// current cache size in bytes
	private static volatile int cachesize = 0;
	// max cache size in bytes
	private static final int MAX_CACHESIZE = 4000000;

	// Let's not load categories a bunch
	private static volatile ArrayList<Category> categories;

	public static final int SUCCESS = 0;
	public static final int JSON_ERROR = -3;
	public static final int API_FAIL = -50;
	public static final int NETWORK_FAIL = -60;
	public static final int AUTH_FAIL = -70;

	// evict according to LRU strategy (least recently used evicted first)
	private static void evictImageCache(int numBytes) {
		if (numBytes >= MAX_CACHESIZE) {
			System.out.println("tried to evictImageCache >= cachesize");
			// evict all the things!
			imagecache.clear();
			return;
		}
		int numBytesFreed = 0;
		int temp = 0;
		while (numBytesFreed < numBytes) {
			// walk imagecache.keySet()
			//       find member w/ min accessTime
			//       evict it
			//       numBytesFreed += size of evicted member
			//       cachesize -= size of evicted member
			temp = evictLeastAccessTime();
			numBytesFreed += temp;
			cachesize -= temp;
			//remove is done in helper
		}
	}

	private static int evictLeastAccessTime()
	{
		CacheItem lruTemp = new CacheItem();
		String tempMinAccessKey = "";
		int tempFreed = 0;
		for(Map.Entry<String, CacheItem> entry : imagecache.entrySet()) {
			lruTemp = imagecache.entrySet().iterator().next().getValue();
			tempMinAccessKey = entry.getKey(); //this is key of "first" element in HashMap
			tempFreed = entry.getValue().bytes.length;
			break;
		}
		long tempMinAccess = lruTemp.numAccess; //tempMinAccess is the numAccess of "first" element in HashMap

		//Map.Entry<String, CacheItem> entry : imagecache.entrySet();
		Iterator<Map.Entry<String, CacheItem>> it = imagecache.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, CacheItem> entry = it.next();
			if(entry.getValue().accessTime < tempMinAccess) {
				tempMinAccess = entry.getValue().accessTime;
				tempMinAccessKey = entry.getKey();
				tempFreed = entry.getValue().bytes.length;
			}
		}
		//at this point we should have LRU key in tempMinAccessKey and LRU minAccess in tempMinAccess
		System.out.println("tempMinAccess = " + tempMinAccess);
		System.out.println("tempMinAccessKey = " + tempMinAccessKey);
		imagecache.remove(tempMinAccessKey);
		return tempFreed;
	}

	private void registerMapperSerializers() {
		SimpleModule module = new SimpleModule("DirectionModule", new Version(1,0,0,null));
		module.addSerializer(Direction.class, new DirectionSerializer());
		mapper.registerModule(module);
	}

	private void initMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			registerMapperSerializers();
			mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			mapper.getJsonFactory().configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
		}
	}

	public Comm() {
		System.out.println("Creating new Comm");
		lastJSON = "";
		initMapper();
		if (categories == null) {
			categories = new ArrayList<>();
		}
		if (imagecache == null) {
			imagecache = new HashMap<>();
			cachesize = 0;
		}
	}

	public static User getUser() {
		return user;
	}

	public int getUserID() {
		if (user == null) {
			return -1;
		}
		return user.id;
	}

	public static int staticGetUserID() {
		System.out.println("Comm.getEmail() => " + user.id);
		return user.id;
	}

	public static String getEmail() {
		System.out.println("Comm.getEmail() => " + user.username);
		return user.username;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getLastJSON() {
		return lastJSON;
	}

	public static void prettyPrint(String s) {
		try {
			Object json = mapper.readValue(s, Object.class);
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
		} catch (Exception e) {
			System.out.println("EXCEPTION in prettyPrint");
		}
	}
	public static void prettyPrint(JsonNode s) {
		try {
			Object json = mapper.readValue(s, Object.class);
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
		} catch (Exception e) {
			System.out.println("EXCEPTION in prettyPrint");
		}
	}

	public static void main(String[] args) {
		runningAndroid = false;
		Comm c = new Comm();
		c.login("test@test.net", "test1234");

		c.getCategories();

		c.newAccount("bob@test.net", "bobhasbadpasswords");
		System.out.println("c.login returns " + c.login("bob@test.net", "bobhasbadpasswords"));
		System.out.println("c.login returns " + c.login("bob@test.net", "bobhasGOODpasswords"));

		c.searchRecipes("soup");
		Recipe simple = c.getRecipe(1);
		ArrayList<Ingredient> list = simple.ingredients;
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i + " " + list.get(i).amount + " " + list.get(i).name);
		}
		ArrayList<Direction> dirs = simple.directions;
		for (int i = 0; i < dirs.size(); i++) {
			System.out.println(i + " " + dirs.get(i).text);
		}
		System.out.println("now display the categories");
		ArrayList<Integer> cats = simple.categories;
		for(int i = 0; i < cats.size(); i++) {
			System.out.println(i + " " + cats.get(i));
		}
		if(simple.categories.size() == 0)
			System.out.println("there are no categories");
	}

	public int newAccount(String email, String password) {
		HashMap<String, String> req = new HashMap<>();
		req.put("email", email);
		req.put("password", password);
		return apiRequest("signup", req);
	}

	public int logout() {
		HashMap<String, String> req = new HashMap<>();
		req.put("uid", Integer.toString(user.id));
		return apiRequest("logout", req);
	}

	private void parseUser(JsonNode un) {
		try {
			System.out.println("Comm.parseUser()");
			prettyPrint(un);

			User u = new User();
			u.id = mapper.readValue(un.path("id"), Integer.class);
			u.username = mapper.readValue(un.path("username"), String.class);
			//debugging_system_out
			System.out.println("u.id = " + u.id);
			System.out.println("u.username = " + u.username);
			Iterator<JsonNode> ite;

			ite = un.path("recipes").getElements();
			while (ite.hasNext()) {
				u.recipes.add(ite.next().getIntValue());
			}

			ite = un.path("drafts").getElements();
			while (ite.hasNext()) {
				u.drafts.add(ite.next().getIntValue());
			}

			ite = un.path("followers").getElements();
			while (ite.hasNext()) {
				u.followers.add(ite.next().getIntValue());
			}

			ite = un.path("following").getElements();
			while (ite.hasNext()) {
				u.following.add(ite.next().getIntValue());
			}

			ite = un.path("favorites").getElements();
			while (ite.hasNext()) {
				u.favorites.add(ite.next().getIntValue());
			}

			ite = un.path("notifications").getElements();
			while (ite.hasNext()) {
				u.notifications.add(ite.next().getIntValue());
			}

			user = u;
		} catch (Exception e) {
			System.out.println("exception in Comm.parseUser: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// gets new user info from the server
	// including notifications, drafts, recipes, etc
	public void updateUser() {
		HashMap<String, String> req = new HashMap<>();
		req.put("uid", Integer.toString(user.id));
		int ret = apiRequest("userinfo", req);
		if (lastStatus == 1) {
			parseUser(rootNode.path("user"));
		}
	}

	public int login(String email, String password) {
		HashMap<String, String> req = new HashMap<>();
		req.put("email", email);
		req.put("password", password);
		System.out.println("completed req.put email and password. about to call apiRequest");
		int ret = apiRequest("login", req);

		if (ret == 0) {
			try {
				if (lastStatus == 1) {
					//debugging_system_out
					System.out.println("last status == 1 if statement entered");
					String token = mapper.readValue(rootNode.path("token"), String.class);
					authToken = token;
					//debugging_system_out
					System.out.println("about to call parserUser");
					parseUser(rootNode.path("user"));
					//debugging_system_out
					System.out.println("done parseUser");
					System.out.println("user.id = " + user.id);
					System.out.println("user.username = " + user.username);

					getCategories();
					return SUCCESS;
				} else {
					//debugging_system_out
					System.out.println("returning API FAIL");
					return API_FAIL;
				}
			} catch (Exception e) {
				//debugging_system_out
				System.out.println("return json error login fail");
				System.out.println("LOGIN FAIL: ");
				e.printStackTrace();
				return JSON_ERROR;
			}
		} else {
			//debugging_system_out
			System.out.println("return ret in login");
			return ret;
		}
	}

	public ArrayList<Recipe> searchRecipes(String search) {
		HashMap<String, String> req = new HashMap<>();
		req.put("keyword", search);
		apiRequest("search", req);

		// 		 process whatever JSON we are handed back and
		//       spin up some Recipe objects, fill them in
		//       and return those
		ArrayList<Recipe> ls = new ArrayList<>();
		Iterator<JsonNode> ite = rootNode.path("recipes").getElements();
		while(ite.hasNext())
		{
			JsonNode r = ite.next();
			prettyPrint(r);
			ls.add(parseRecipe(r, true));
		}
		return ls;
	}

	public ArrayList<Recipe> searchRecipesByCategory(int categoryID) {
		HashMap<String, Integer> req = new HashMap<>();
		req.put("categoryID", categoryID);
		apiRequest("searchcat", req);

		// 		 process whatever JSON we are handed back and
		//       spin up some Recipe objects, fill them in
		//       and return those
		ArrayList<Recipe> ls = new ArrayList<>();
		Iterator<JsonNode> ite = rootNode.path("recipes").getElements();
		while(ite.hasNext())
		{
			JsonNode r = ite.next();
			ls.add(parseRecipe(r, true));
		}
		return ls;
	}

	public ArrayList<Recipe> searchRecipesByUID(int userID) {
		HashMap<String, Integer> req = new HashMap<>();
		req.put("uid", userID);
		apiRequest("searchuid", req);

		if (lastStatus == 1) {
			ArrayList<Recipe> ls = new ArrayList<>();
			Iterator<JsonNode> ite = rootNode.path("recipes").getElements();
			while(ite.hasNext())
			{
				JsonNode r = ite.next();
				ls.add(parseRecipe(r, true));
			}
			return ls;
		}
		return new ArrayList<>();
	}

	public ArrayList<Recipe> getAllDrafts(int userID) {
		HashMap<String, Integer> req = new HashMap<>();
		req.put("uid", userID);
		apiRequest("getalldrafts", req);

		if (lastStatus == 1) {
			ArrayList<Recipe> ls = new ArrayList<>();
			Iterator<JsonNode> ite = rootNode.path("recipes").getElements();
			while (ite.hasNext()) {
				JsonNode r = ite.next();
				ls.add(parseRecipe(r, false));
			}
			return ls;
		}
		return new ArrayList<>();
	}

	public ArrayList<Recipe> getAllNotifs(int userID) {
		HashMap<String, Integer> req = new HashMap<>();
		req.put("uid", userID);
		apiRequest("getallnotifs", req);

		if (lastStatus == 1) {
			ArrayList<Recipe> ls = new ArrayList<>();
			Iterator<JsonNode> ite = rootNode.path("recipes").getElements();
			while (ite.hasNext()) {
				JsonNode r = ite.next();
				ls.add(parseRecipe(r, false));
			}
			return ls;
		}
		return new ArrayList<>();
	}

	private Bitmap pngToBitmap(byte[] bytes) {
		if (bytes == null) {
			System.out.println("Comm.pngToBitmap got a null bytes");
			return null;
		}
		Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		if (bmp == null) {
			System.out.println("Comm.pngToBitmap failed to decode byte array of length " + bytes.length);
		}
		return bmp;
	}

	private Bitmap getImage(String relUrl) {
		System.out.println("getImage(" + serverImgRoot + relUrl);
		if (relUrl == null) {
			System.out.println("tried to get a null-url image");
			return null;
		}

		if (imagecache.containsKey(relUrl)) {
			System.out.println("Comm.getImage using cached png... cache size in bytes is " + cachesize);
			CacheItem ci = imagecache.get(relUrl);
			ci.accessTime = System.currentTimeMillis();
			ci.numAccess++;
			System.out.println("  CacheItem.numAccess = " + ci.numAccess);
			return pngToBitmap(ci.bytes);
		}

		try {
			URL url = new URL(serverImgRoot + relUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setRequestMethod("GET");

			System.out.println("getImage sees code " + connection.getResponseCode());
			if (connection.getResponseCode() != 200) {
				connection.disconnect();
				return null;
			}

			int len = connection.getContentLength();
			System.out.println("getImage sees content-length " + len);
			String type = connection.getContentType();
			System.out.println("getImage sees content-type " + type);
			Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
			if (bitmap == null) {
				System.out.println("BitmapFactory failed to decode PNG from stream");
				connection.disconnect();
				return null;
			} else {
				connection.disconnect();

				// matt - let's cheat and recompress the png :)
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
				CacheItem ci = new CacheItem();
				ci.accessTime = System.currentTimeMillis();
				ci.bytes = stream.toByteArray();
				ci.numAccess = 0;
				if (ci.bytes.length + cachesize > MAX_CACHESIZE) {
					evictImageCache(ci.bytes.length - (MAX_CACHESIZE - cachesize));
				}
				imagecache.put(relUrl, ci);
				cachesize += ci.bytes.length;

				return bitmap;
			}
		} catch (Exception e) {
			System.out.println("Error in getImage");
			e.printStackTrace();
			System.out.println("getImage failed network");
			return null;
		}
	}

	// returns a new URL for the uploaded image, or "" on failure
	public String imageUpload(Bitmap bmp) {
		System.out.println("imageUpload");
		if (bmp == null) {
			System.out.println("tried to upload a null image -- bailing");
			return "";
		}
		try {
			HashMap<String, Object> o = new HashMap<>();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp = Bitmap.createScaledBitmap(bmp, 1024, 768, false);
			if (bmp.compress(Bitmap.CompressFormat.PNG, 90, stream)) {
				apiRequestBytePayload("imageupload", stream.toByteArray());
				if (lastStatus == 1) {
					String url = mapper.readValue(rootNode.path("image_url"), String.class);
					updateUser();
					return url;
				} else {
					return "";
				}
			} else {
				return "";
			}
		} catch (Exception e) {
			System.out.println("Error in imageUpload");
			e.printStackTrace();
		}
		return "";
	}

	private void parseDirections(Recipe r, String json) {
		try {
			System.out.println("parseDirections got json \"" + json + "\"");
			JsonNode node = mapper.readTree(json);
			Iterator<JsonNode> ite = node.getElements();
			while (ite.hasNext()) {
				JsonNode dir = ite.next();
				String text = mapper.readValue(dir.path("text"), String.class);
				Iterator<JsonNode> ite2 = dir.path("img").getElements();
				ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();
				while(ite2.hasNext()) {
					JsonNode img = ite2.next();
					String img_url = img.getTextValue();
					System.out.println("parseDirections found img_url " + img_url);
					Bitmap bmp = getImage(img_url);
					bmps.add(bmp);
				}

				r.addDirection(text, bmps);
			}
		} catch (Exception e) {
			System.out.println("Error in parseDirections - mapper.readTree or mapper.readValue");
			e.printStackTrace();
		}
	}

	private void parseComments(Recipe r, JsonNode node) {
		if (node.isMissingNode()) {
			return;
		}
		try {
			if (!node.path("rating").isMissingNode()) {
				Float rRating = mapper.readValue(node.path("rating"), Float.class);
				r.rating = rRating.floatValue();
				Iterator<JsonNode> ite = node.path("comments").getElements();
				while(ite.hasNext()) {
					JsonNode cnode = ite.next();
					System.out.print("cnode: ");
					prettyPrint(cnode);
					Integer rating = mapper.readValue(cnode.path("rating"), Integer.class);
					String comment = mapper.readValue(cnode.path("comment"), String.class);
					String username = mapper.readValue(cnode.path("username"), String.class);
					r.comments.add(new Comment(r.id, rating.intValue(), comment, username));
				}
			}
		} catch (Exception e) {
			System.out.println("parseRecipe had an exception parsing comments");
			e.printStackTrace();
		}
	}

	private void parseQuestions(Recipe r, JsonNode node) {
		if (node.isMissingNode()) {
			return;
		}
		try {
			Iterator<JsonNode> ite = node.path("questions").getElements();
			while(ite.hasNext()) {
				JsonNode qnode = ite.next();
				System.out.print("qnode: ");
				prettyPrint(qnode);

				Integer qid = mapper.readValue(qnode.path("qid"), Integer.class);
				Integer quid = mapper.readValue(qnode.path("uid"), Integer.class);
				String qusername = mapper.readValue(qnode.path("username"), String.class);
				String qtext = mapper.readValue(qnode.path("question"), String.class);

				ArrayList<Question> replies = new ArrayList<Question>();
				Iterator<JsonNode> iter = qnode.path("replies").getElements();
				while (iter.hasNext()) {
					JsonNode rnode = iter.next();
					System.out.print("rnode: ");
					prettyPrint(rnode);
					Integer uid = mapper.readValue(rnode.path("uid"), Integer.class);
					String username = mapper.readValue(rnode.path("username"), String.class);
					String text = mapper.readValue(rnode.path("reply"), String.class);
					System.out.println("Parsing reply " + text);

					replies.add(new Question(uid, username, text));
				}

				if (r.questions == null) {
					r.questions = new ArrayList<Question>();
				}
				r.questions.add(new Question(qid, quid, qusername, qtext, replies));
			}
		} catch (Exception e) {
			System.out.println("parseQuestions had an exception parsing questions");
			e.printStackTrace();
		}
	}

	private Recipe parseRecipe(JsonNode node) {
		return parseRecipe(node, false);
	}

	private Recipe parseRecipe(JsonNode node, boolean brief) {
		Recipe r = null;

		try {
			Integer id = mapper.readValue(node.path("rid"),Integer.class);
			String description = mapper.readValue(node.path("description"), String.class);
			String image_url = mapper.readValue(node.path("img_url"), String.class);
			if (image_url.length() == 0) {
				image_url = "default.png";
			}
			String name = mapper.readValue(node.path("name"), String.class);
			System.out.println("parseRecipe for image_url " + image_url);
			r = new Recipe(id, name, description, getImage(image_url));
			String cooktime = mapper.readValue(node.path("cooktime"), String.class);
			r.setCookTime(cooktime);
			Integer uid = mapper.readValue(node.path("uid"), Integer.class);
			r.uid = uid;
			String username = mapper.readValue(node.path("username"), String.class);
			r.username = username;

			if (!node.path("did").isMissingNode()) {
				Integer draftID = mapper.readValue(node.path("did"), Integer.class);
				r.did = draftID;
			}

			if (!node.path("mainImagepath").isMissingNode()) {
				r.mainImagepath = mapper.readValue(node.path("mainImagepath"), String.class);
			} else {
				r.mainImagepath = "";
			}

			if (!brief) {
				String ingredientsJson = mapper.readValue(node.path("ingredients"), String.class);
				r.parseIngredientsFromJson(ingredientsJson);
				String directionsJson = mapper.readValue(node.path("directions"), String.class);
				parseDirections(r, directionsJson);
				String categoriesJson = mapper.readValue(node.path("categories"), String.class);
				r.parseCategoriesFromJson(categoriesJson);
				JsonNode commentsNode = node.path("comments");
				parseComments(r, commentsNode);
				JsonNode questionsNode = node.path("questions");
				parseQuestions(r, questionsNode);
			}

			return r;
		} catch (Exception e) {
			System.out.println("Error in parseRecipe - mapper.readValue");
			e.printStackTrace();
		}
		return null;
	}

	public Recipe getRecipe(int recipeID) {
		HashMap<String, String> req = new HashMap<>();
		req.put("rid", Integer.toString(recipeID));
		apiRequest("get", req);

		if (rootNode.path("recipe").isMissingNode()) {
			System.out.println("getRecipe() got null recipe -- returning blank");
			return new Recipe();
		}
		return parseRecipe(rootNode.path("recipe"));
	}

	public int clearNotifications() {
		apiRequest("clearnotifications", null);
		if (lastStatus == 1) {
			updateUser();
			return SUCCESS;
		} else {
			return API_FAIL;
		}
	}

	public int uploadRecipe(Recipe r) {
		HashMap<String, Object> req = new HashMap<>();
		req.put("uid", Integer.toString(user.id));
		HashMap<String, Object> recipe = new HashMap<>();
		recipe.put("rid", r.id);
		recipe.put("name", r.name);
		recipe.put("description", r.description);
		recipe.put("cooktime", r.cookTime);
		System.out.println("uploadRecipe uploading main image");
		if (r.mainImage == null && r.mainImagepath != null && r.mainImagepath != "") {
			r.loadImageFromPath();
		}
		recipe.put("image_url", imageUpload(r.mainImage));
		recipe.put("categories", r.categories);
		recipe.put("ingredients", r.ingredients);
		recipe.put("directions", r.directions);
		try {
			recipe.put("parseddirs", mapper.writeValueAsString(r.directions));
		} catch (Exception e) {
			System.out.println("parseddirs serialization failed");
			e.printStackTrace();
		}

		req.put("recipe", recipe);

		apiRequest("uploadrecipe", req);

		if (lastStatus == 1) {
			updateUser();
			return SUCCESS;
		} else {
			return API_FAIL;
		}
	}

	public int postQuestion(int recipeId, String question) {
		HashMap<String, String> req = new HashMap<>();
		req.put("uid", Integer.toString(user.id));
		req.put("rid", Integer.toString(recipeId));
		req.put("question", question);
		int ret = apiRequest("postquestion", req);
		if (ret == 0) {
			if (lastStatus == 1) {
				updateUser();
				return SUCCESS;
			} else {
				return API_FAIL;
			}
		} else {
			return ret;
		}
	}

	public int postReply(int questionId, String reply) {
		HashMap<String, String> req = new HashMap<>();
		req.put("uid", Integer.toString(user.id));
		req.put("qid", Integer.toString(questionId));
		req.put("reply", reply);
		int ret = apiRequest("postreply", req);
		if (ret == 0) {
			if (lastStatus == 1) {
				updateUser();
				return SUCCESS;
			} else {
				return API_FAIL;
			}
		} else {
			return ret;
		}
	}

	public int follow(int uid) {
		HashMap<String, String> req = new HashMap<>();
		req.put("uid", Integer.toString(uid));
		apiRequest("follow", req);
		if (lastStatus == 1) {
			updateUser();
			return SUCCESS;
		} else {
			return API_FAIL;
		}
	}

	public int addFavorite(int recipeID) {
		HashMap<String, String> req = new HashMap<>();
		req.put("rid", Integer.toString(recipeID));
		apiRequest("addfavorite", req);
		if (lastStatus == 1) {
			updateUser();
			return SUCCESS;
		} else {
			return API_FAIL;
		}
	}

	public int postComment(Comment c) {
		HashMap<String, String> req = new HashMap<>();
		req.put("uid", Integer.toString(user.id));
		req.put("rid", Integer.toString(c.recipeID));
		req.put("rating", Integer.toString(c.rating));
		req.put("comment", c.comment);
		int ret = apiRequest("rate", req);
		if (ret == 0) {
			if (lastStatus == 1) {
				updateUser();
				return SUCCESS;
			} else {
				return API_FAIL;
			}
		} else {
			return ret;
		}
	}

	public int saveDraft(Recipe r) {
		HashMap<String, Object> req = new HashMap();
		req.put("uid", Integer.toString(user.id));
		HashMap<String, Object> recipe = new HashMap<>();
		recipe.put("rid", Integer.toString(r.id));
		recipe.put("name", r.name);
		recipe.put("description", r.description);
		recipe.put("cooktime", r.cookTime);
		System.out.println("savingDraft uploading main image");
		if (r.mainImage == null && r.mainImagepath != null && r.mainImagepath != "") {
			r.loadImageFromPath();
		}
		recipe.put("image_url", imageUpload(r.mainImage));
		recipe.put("mainImagepath", r.mainImagepath);
		recipe.put("categories", r.categories);
		System.out.println("Comm.saveDraft got categories " + r.toString());
		recipe.put("ingredients", r.ingredients);

		recipe.put("directions", r.directions);
		try {
			recipe.put("parseddirs", mapper.writeValueAsString(r.directions));
		} catch (Exception e) {
			System.out.println("parseddirs serialization failed");
			e.printStackTrace();
		}

		recipe.put("did", Integer.toString(r.did));

		req.put("recipe", recipe);
		int ret;
		if (r.did != 0 && r.did != -1) {

			ret = apiRequest("updatedraft", req);
		} else {
			ret = apiRequest("savedraft", req);
		}
		if(ret == 0) {
			if(lastStatus == 1) {
				updateUser();
				return SUCCESS;
			} else {
				return API_FAIL;
			}
		} else {
			return ret;
		}
	}

	// get the list of draft ids for the current user
	//   I guess we don't need this, because we are storing that in our User object
	public ArrayList<Integer> getDraftList() {
		return user.drafts;
	}

	public Recipe getDraft(int draftID) {
		HashMap<String, String> req = new HashMap<>();
		req.put("did", Integer.toString(draftID));
		apiRequest("getdraft", req);

		Recipe r = parseRecipe(rootNode.path("recipe"));
		if (r.mainImagepath == null) {
			System.out.println("r.mainImagepath was null, making it empty");
			r.mainImagepath = "";
		}
		System.out.println("Comm.getDraft(" + draftID + ") has categories " + r.categories.toString());
		System.out.println("Comm.getDraft(" + draftID + ") has the mainImagepath = " + r.mainImagepath.toString());
		r.did = draftID;
		return r;
	}

	public int deleteDraft(int draftID) {
		HashMap<String, String> req = new HashMap<>();
		req.put("did", Integer.toString(draftID));
		apiRequest("deletedraft", req);

		if (lastStatus == 1) {
			updateUser();
			return SUCCESS;
		} else {
			return API_FAIL;
		}
	}


	/*
	 * getCategories returns a pre-sorted list of categories.
	 *   ie Each top level category is followed by its children,
	 *      and they are followed by their children in hierarchical order.
	 */
	public ArrayList<Category> getCategories() {
		if (categories != null && categories.size() > 0) {
			return categories;
		}

		ArrayList<Category> cats = new ArrayList<>();
		int ret = apiRequest("categories", null);
		if (ret == 0) {
			if (lastStatus == 1) {
				Iterator<JsonNode> ite = rootNode.path("categories").getElements();
				while(ite.hasNext())
				{
					JsonNode r = ite.next();
					try {
						Integer id = mapper.readValue(r.path("id"),Integer.class);
						String name = mapper.readValue(r.path("name"), String.class);
						Integer level = mapper.readValue(r.path("level"), Integer.class);
						cats.add(new Category(id.intValue(), level.intValue(), name));
					} catch (Exception e) {
						System.out.println("failed in getCategories - mapper.readValue");
						e.printStackTrace();
						return null;
					}
				}
				updateUser();
				categories = cats;
				return cats;
			} else {
				return null;
			}
		}
		return null;
	}

	private int apiRequest(String relUrl, Object o) {
		if (o == null) {
			return apiRequestPayload(relUrl, "");
		}
		try {
			if (mapper == null) {
				System.out.println("apiRequest has a null mapper");
			}
			return apiRequestPayload(relUrl, mapper.writeValueAsString(o));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("died writing value string " + o);
			return JSON_ERROR;
		}
	}

	private int apiRequestBytePayload(String relUrl, byte[] payload) {
		String line;
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL(serverRoot + relUrl);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.setRequestProperty("uid", Integer.toString(Comm.user.id));
			connection.setRequestProperty("token", Comm.authToken);
			OutputStream os = connection.getOutputStream();
			os.write(payload);
			os.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
			connection.disconnect();
			System.out.println(jsonString);
			lastJSON = jsonString.toString();
			rootNode = mapper.readTree(lastJSON);
			lastStatus = API_FAIL;
			try {
				Integer status = mapper.readValue(rootNode.path("status"), Integer.class);
				lastStatus = status;
				return SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed in apiRequest : fail to readValue from \"status\" ");
				return API_FAIL;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("failed in apiRequest: failed URL");
			return NETWORK_FAIL;
		}
	}

	private int apiRequestPayload(String relUrl, String payload) {
		String line;
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL(serverRoot + relUrl);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			if (Comm.user != null) {
				connection.setRequestProperty("uid", Integer.toString(Comm.user.id));
			}
			connection.setRequestProperty("token", Comm.authToken);
			connection.setRequestProperty("commversion", Integer.toString(Comm.commVersion));
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			writer.write(payload);
			writer.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
			connection.disconnect();
			System.out.println(jsonString);
			lastJSON = jsonString.toString();
			rootNode = mapper.readTree(lastJSON);
			lastStatus = API_FAIL;
			try {
				Integer status = mapper.readValue(rootNode.path("status"), Integer.class);
				lastStatus = status;
				if (lastStatus == -1) {
					return AUTH_FAIL;
				} else {
					return SUCCESS;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed in apiRequest : fail to readValue from \"status\" ");
				return API_FAIL;
			}
		} catch (Exception e) {
			System.out.println("apiRequestPayload caught an exception: " + e.getMessage());
			e.printStackTrace();
			return NETWORK_FAIL;
		}
	}
}
