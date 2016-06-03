package net.nashlegend.demo;

/**
 * Created by NashLegend on 16/6/2.
 */
public class SampleSub {

    public String name = "father";

    public Sample sample;//如果SampleSub也有一个标记了PrefSub的Sample，就发生死循环
}
