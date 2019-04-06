package cn.wlx.codejudger.node.docker;

public class DockerEvent {
  String status;
  String id;
  String from;
  String type;
  String Action;
  Actor Actor;
  String scope;
  String time;
  String timeNano;
  public class Actor {
    String ID;
    Attributes Attributes;
    public class Attributes {
      String image;
      String name;
    }
  }
}
