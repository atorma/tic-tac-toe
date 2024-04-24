package org.atorma.tictactoe.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class FaviconController {

    @RequestMapping(path = "/favicon.ico", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnNoFavicon() {
    }
}
