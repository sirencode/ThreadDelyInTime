package com.mythread.diablo.threaddelyintime.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者： shenyonghe689 on 16/4/12.
 */
public class StringUtil
{
    public static boolean  isNullOrEmpty(String info){
        if (info==null){
            return true;
        }
        if(info.equals("")){
            return true;
        }
        return false;
    }


    public static int getSize(String info){
        return info.length();
    }

    public static boolean startWith(String info,String start){
        return info.startsWith(start);
    }

    public static boolean isTheType(String info,String regex){
        Pattern pipePattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher pipeMatcher = pipePattern.matcher(info);
        return pipeMatcher.matches();
    }
}
