package com.mythread.diablo.threaddelyintime.http;

/**
 * 作者： Diablo on 15/12/29.
 *
 */
public class BaseKeyValueDto
{
    private String key;

    private String value;

    public BaseKeyValueDto(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }


}
