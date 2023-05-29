package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;


public class TicketMasterAPI {

	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "kGpe6qVPITDTKn7YDoZ9uAX6jpo89nMK";

	//傳入經緯度, 關鍵字來搜索
	public List<Item> search(double lat, double lon, String keyword) {
		if(keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");  //把像是空格轉乘%20  在http request中是個關鍵字
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//String.format把 %s 帶入後面的值
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s", API_KEY, lat, lon, keyword, 50);
		String url = URL + "?" + query;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();  // 測試該url是否會連接成功
			connection.setRequestMethod("GET");  //使用GET送出
			int responseCode = connection.getResponseCode();  //送出請求第一次，看是不是200
			System.out.println("Sending request to url:" + url);
			System.out.println("Response code:" + responseCode);
			
			if(responseCode != 200) {
				return new ArrayList<>();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));//他沒辦法直接傳一個inputstream進來，bufferreader一次讀8192的trunk進來，送出請求第二次
			String line; // 一行一行讀buffer的東西
			StringBuilder response = new StringBuilder();
			while((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			JSONObject obj = new JSONObject(response.toString());
			if (!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");  //JSON解碼
				//return embedded.getJSONArray("events");
				return getItemList(embedded.getJSONArray("events"));
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	//測試function
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);

		for (Item event : events) {
			System.out.println(event.toJSONObject());
		}

	}
	
	private String getAddress(JSONObject event) throws JSONException{    // 從API抓來的JSONObject抓到address項目
		if(!event.isNull("_embedded")) {
			JSONObject  embedded = event.getJSONObject("_embedded");  //此為第二層的embedded tag
			if(!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for(int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder stringBuilder = new StringBuilder();
					if(!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if(!address.isNull("line1")) {
							stringBuilder.append(address.getString("line1"));
						}
						if(!address.isNull("line2")) {
							stringBuilder.append(",");
							stringBuilder.append(address.getString("line1"));
						}
						if(!address.isNull("line3")) {
							stringBuilder.append(",");
							stringBuilder.append(address.getString("line1"));
						}
					}
					if(!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						stringBuilder.append(",");
						stringBuilder.append(city.getString("name"));
					}
					String result = stringBuilder.toString();
					if(!result.isEmpty()) {
						return result;
					}
				}
			}
		}
		return "";
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	
	private Set<String> getCategories(JSONObject event) throws JSONException {
		
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); ++i) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}

	
	private List<Item> getItemList(JSONArray events) throws JSONException{    //把JSONarray轉乘List Item
		List<Item> itemList = new ArrayList<>();
		for(int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			
			if(!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if(!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if(!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if(!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if(!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			builder.setAddress(getAddress(event))
			.setCategories(getCategories(event))
			.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}
		return itemList;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}

}