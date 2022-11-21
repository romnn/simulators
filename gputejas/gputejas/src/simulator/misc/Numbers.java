/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Prathmesh Kallurkar
*****************************************************************************/

package misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Numbers {

	static public long hexToLong(String hexStr)
	{
		try {
			long num = 0, pow = 1;
			byte []numBytes = hexStr.getBytes(); 
			for(int i=hexStr.length()-1; i>=0; i--, pow*=16) {
				if(numBytes[i]>='0' && numBytes[i]<='9') {
					num += pow * (numBytes[i]-'0');
				} else if(numBytes[i]>='a' && numBytes[i]<='f') {
					num += pow * (10 + numBytes[i]-'a');
				} else if(numBytes[i]>='A' && numBytes[i]<='F') {
					num += pow * (10 + numBytes[i]-'A');
				} else if(numBytes[i]=='x' || numBytes[i]=='X') {
					num += 0; // 0x or 0X for hex numbers
				} else {
					throw new NumberFormatException();
				}
			}
			
			return num;
		} catch (NumberFormatException nfe) {
			misc.Error.showErrorAndExit("incorrect number string : " + hexStr);
			return (-1);
		}
	}
	
	private static Matcher validNumberMatcher;
	public static void createValidNumberMatcher()
	{
		Pattern p = Pattern.compile("[0xX0-9a-fA-F]+");
		validNumberMatcher = p.matcher("");
	}
	
	static public boolean isValidNumber(String numStr)
	{
		if(validNumberMatcher==null) {
			createValidNumberMatcher();
		}
		
		if(numStr==null) {
			return false;
		} else {
			if(validNumberMatcher.reset(numStr).matches()) {
				return true;
			} else {
				return false;
			}
		}
		
//		//Remove the 0x prefix
//		if(numStr.length()>2 && numStr.substring(0,2).contentEquals("0x"))
//			numStr = numStr.substring(2);
//		
//		//If conversion from string to number generates an exception then 
//		// the string probably ain't a valid number
//		try{
//			Long.parseLong(numStr,16);
//			return true;
//		}catch(NumberFormatException nfe){
//			return false;
//		}
	}
}
