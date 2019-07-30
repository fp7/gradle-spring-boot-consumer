package io.github.fp7.gradle.example;

import io.github.fp7.gradle_spring_boot_consumer.Main;

public class Example {
  public static String foo(){
    return Main.main();
  }

  public static void main(String[] args) {
    System.out.println(foo());
  }
}