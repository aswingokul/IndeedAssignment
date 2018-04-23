/**
 * 
 */
package indeed.assignment;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * @author aswin
 *
 */
public class LocalStorage {
	
	JavascriptExecutor jsExec = null;
	
	public LocalStorage(WebDriver wd){
		this.jsExec = (JavascriptExecutor) wd;
	}
	
	public String getItem(String key){
		 return (String) jsExec.executeScript(String.format("return window.localStorage.getItem('%s')", key));
	}
	
	public void setItem(String key, String value){
		jsExec.executeScript(String.format("window.localStorage.setItem('%s', '%s');", key, value));
	}
	
	public boolean checkKeyExists(String key){
		return (Boolean) (jsExec.executeScript(String.format("window.localStorage.getItem('%s')", key)) == null);
	}

}
