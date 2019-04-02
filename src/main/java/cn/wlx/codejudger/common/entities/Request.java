package cn.wlx.codejudger.common.entities;

import cn.wlx.codejudger.common.enumerate.RequestType;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Request {

  public RequestType requestType;
  public String contents;
}
