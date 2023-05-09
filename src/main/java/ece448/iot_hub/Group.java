package ece448.iot_hub;

import java.util.List;

public class Group {
   
   private String name;
   private List<String> plugNames;
   
   public Group(String name, List<String> plugNames) {
       this.name = name;
       this.plugNames = plugNames;
   }

   public String getName() {
       return name;
   }

   public List<String> getPlugNames() {
       return plugNames;
   }

   public void setPlugNames(List<String> plugNames) {
       this.plugNames = plugNames;
   }
}