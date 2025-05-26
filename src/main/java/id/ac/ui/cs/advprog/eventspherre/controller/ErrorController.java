package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    
    private static final String ERROR_CODE_ATTRIBUTE = "errorCode";
    private static final String ERROR_MESSAGE_ATTRIBUTE = "errorMessage";
    
    @GetMapping("/unauthorized")
    public String unauthorized() {
        return AppConstants.VIEW_UNAUTHORIZED;
    }    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
              if (statusCode == AppConstants.HTTP_STATUS_NOT_FOUND) {
                model.addAttribute(ERROR_CODE_ATTRIBUTE, String.valueOf(AppConstants.HTTP_STATUS_NOT_FOUND));
                model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, AppConstants.ERROR_PAGE_NOT_FOUND);
            } else if (statusCode == AppConstants.HTTP_STATUS_FORBIDDEN) {
                model.addAttribute(ERROR_CODE_ATTRIBUTE, String.valueOf(AppConstants.HTTP_STATUS_FORBIDDEN));
                return AppConstants.VIEW_UNAUTHORIZED;
            } else if (statusCode == AppConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR) {
                model.addAttribute(ERROR_CODE_ATTRIBUTE, String.valueOf(AppConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR));
                model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, AppConstants.ERROR_INTERNAL_SERVER);
            }
        } else {
            model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, AppConstants.ERROR_UNEXPECTED);
        }
        
        return AppConstants.VIEW_ERROR;
    }
}