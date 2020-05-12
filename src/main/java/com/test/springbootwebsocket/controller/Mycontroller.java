package com.test.springbootwebsocket.controller;

import com.test.springbootwebsocket.service.WebSocketServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.websocket.server.PathParam;
import java.io.IOException;

@RestController
@CrossOrigin
public class Mycontroller {
    /**
     * 页面请求
     */
    @GetMapping("/socket/{uid}")
    public ModelAndView socket(@PathVariable String uid) {
        ModelAndView mav = new ModelAndView("/socket");
        mav.addObject("uid", uid);
        return mav;
    }

    /**
     * 推送数据接口
     */
    @RequestMapping("/socket/push/{uid}")
    public String pushToWeb(@PathVariable String uid, String message) {
        try {
            WebSocketServer.sendInfo(message, uid);
        } catch (IOException e) {
            e.printStackTrace();
            return "推送失败";
        }
        return "推送成功";
    }
}
