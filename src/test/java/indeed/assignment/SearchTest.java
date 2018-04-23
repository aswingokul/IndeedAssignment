/**
 * 
 */
package indeed.assignment;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import java.util.List;

import java.util.Stack;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.html5.RemoteLocalStorage;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.bytebuddy.dynamic.scaffold.InstrumentedType.Frozen;

/**
 * @author aswin
 *
 */
public class SearchTest {

	WebDriver driver = null;
	LocalStorage local;
	Gson gson = new Gson();
	Stack<JsonElement> stack = new Stack<JsonElement>();

	@Test
	public void driverMethod() throws InterruptedException {

		System.setProperty("webdriver.chrome.driver", "/Users/aswin/Downloads/chromedriver");
		driver = new ChromeDriver();
		driver.manage().window().fullscreen();
		driver.get("https://www.indeed.com/resumes");

		// Queries
		querySite("software engineer", "california, united states");
		querySite("qa engineer", "Toronto, canada");
		querySite("network admin", "chennai, TN");
		querySite("Content Writer", "Mumbai");
		querySite("Senior Systems Engineer", "North Carolina");
		querySite("Story Writer", "Mexico");
		querySite("Sales Manager", "Delhi");
		querySite("Chip Engineer", "Texas");
		querySite("iOS developer", "Berlin");
		querySite("Android developer", "Paris");
		querySite("Principal Engineer", "Seattle");
		querySite("Site Engineer", "New York");
		querySite("Ruby on Rails", "Delhi");
		
		Thread.sleep(2000);
		
		goHome();
		
//		querySite("", "");
		getSearchItems("#recent-searches-footer ul");

		clear();

		Thread.sleep(3000);

		

	}

	@AfterTest
	public void close() {
		driver.close();
	}
	
	public void goHome(){
		driver.findElement(By.id("logo_link")).click();
		modifySearches(true);
	}

	/*
	 * Util function to get query string
	 */
	public String getQueries(String url) {
		try {
			URL obj = new URL(url);
			String queries = obj.getQuery();
			return queries;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Limit the json array size to 10 or 5 based on the page
	 */
	public void modifySearches(boolean home) {
		System.out.println("Modify Searches......");
		String searches = getSearchRes();

		JsonArray searchObj = toJsonArray(searches);
		System.out.println("searchObj: " + searchObj.toString() );

		// If the size is more than 10, remove the earlier searches
		if (home) {
			System.out.println("In Home page!!");
			if (searchObj.size() < 10 && !stack.isEmpty()) {
				System.out.println("Searches are < 10... so adding some more...");
				List<JsonElement> elemList = jsonArrayToList(searchObj);
				System.out.println("Before elemList size: " + elemList.size());
				while (elemList.size() < 10 && !stack.isEmpty()) {					
					elemList.add(stack.pop());
					
				}
				System.out.println("After elemList size: " + elemList.size());
				String element = gson.toJson(elemList, new TypeToken<List<JsonElement>>() {}.getType());
				System.out.println("element:");
				System.out.println(element);
				JsonElement converted = new JsonParser().parse(element);
				searchObj = converted.getAsJsonArray();
			} else {
				System.out.println("Searches are > 10... So removing some...");
				while (searchObj.size() > 0 && searchObj.size() > 10) {
					searchObj.remove(searchObj.size()-1);
				}
			}

		} else {
			System.out.println("In results page...");
			while (searchObj.size() > 0 && searchObj.size() > 5) {
				stack.push(searchObj.remove(searchObj.size() - 1));
			}
		}

		for (int i = 0; i < searchObj.size(); i++) {
			JsonObject queryObj = searchObj.get(i).getAsJsonObject();
			System.out.println("i: " + i + " ==> " + queryObj);
		}

		// Store the modified JsonArray back to the key in the local storage
		String modified = searchObj.toString();

		System.out.println("modified: " + modified);
		local.setItem("indeed.rex.serp::.RecentSearches", modified);
		
		driver.navigate().refresh();

	}

	/*
	 * Util function to get the recent searches
	 */
	public String getSearchRes() {
		local = new LocalStorage(driver);
		String searches = local.getItem("indeed.rex.serp::.RecentSearches");
		System.out.println("searches: " + searches);
		return searches;
	}

	public JsonArray toJsonArray(String str) {
		JsonArray searches = (JsonArray) new JsonParser().parse(str);
		return searches;
	}

	/*
	 * Util function to query the site with different what and where values
	 */
	public void querySite(String query, String location) {
		driver.findElement(By.id("query")).clear();
		driver.findElement(By.id("location")).clear();

		// Query 3
		driver.findElement(By.id("query")).sendKeys(query);
		driver.findElement(By.id("location")).sendKeys(location);
		driver.findElement(By.id("submit")).click();
		
		String currUrl = driver.getCurrentUrl();
		
		if(query == ""){
			modifySearches(true);
		}else if(getQueries(currUrl) == null){
			modifySearches(true);
		}else{
			modifySearches(false);
		}
		
		
	}

	/*
	 * This method asserts whether the clear button works as intended
	 */
	public void clear() {
		String currUrl = driver.getCurrentUrl();
		String selector = "";
		if(getQueries(currUrl) == null){
			selector = "#recent-searches-footer a.clear-recent top";			
		}else{
			selector = "#recent-searches > a";
		}
		
		WebElement clear = driver.findElement(By.cssSelector(selector));
		if (clear.isDisplayed()) {
			clear.click();
			Assert.assertTrue(local.checkKeyExists("indeed.rex.serp::.RecentSearches"));

		} else {
			System.out.println("!!!! clear is not visible !!!!");
		}

	}

	/*
	 * Util function to get the list of search items
	 */
	public void getSearchItems(String ulSelector) {
		WebElement ul = driver.findElement(By.cssSelector(ulSelector));
		List<WebElement> searches = driver.findElements(By.cssSelector("li>a"));
		
		searches.get(5).click();
		modifySearches(false);
		
	}

	public List<JsonElement> jsonArrayToList(JsonArray obj) {
		int len = obj.size();
		List<JsonElement> arrList = new ArrayList<JsonElement>();
		if (obj != null) {
			for (int i = 0; i < len; i++) {
				arrList.add(obj.get(i));
			}
		}
		return arrList;
	}

}
