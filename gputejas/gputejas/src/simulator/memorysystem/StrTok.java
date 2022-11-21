package memorysystem;
/*
/*****************************************************************************
				GPUTejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2014] [Indian Institute of Technology, Delhi]
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

	Contributors:  Seep Goel, Geetika Malhotra, Harinder Pal
*****************************************************************************/ 
class StrTok {
  private String remainder;
  private String delimiters;

  public StrTok(String s, String delims)
  {
    remainder = s;
    delimiters = delims;
  }

  public String next(String delims)
  {
    if (delims != null) {
      delimiters = delims;
    }
    int i = 0;
    while (i < remainder.length() && delimiters.indexOf(remainder.charAt(i)) >= 0) {
      i++;
    }
    if (i >= remainder.length()) {
      return null;
    }
    remainder = remainder.substring(i);
    while (i < remainder.length() && delimiters.indexOf(remainder.charAt(i)) == -1) {
      i++;
    }
    String r = remainder.substring(0, i);
    if (i < remainder.length()) {
      i++;
    }
    remainder = remainder.substring(i);
    return r;
  }

  public String next()
  {
    return next(null);
  }
}