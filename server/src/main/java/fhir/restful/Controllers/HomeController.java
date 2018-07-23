package fhir.restful.Controllers;

import fhir.restful.Database.Datum;
import fhir.restful.Database.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Defines pages by searching for returned string in the resources/templates directory.
 * Making changes here will alter the home page.
 * The "Model" parameter can be given attributes which can be referenced in the html
 * Thymeleaf provides the ability to reference and use the attributes.
 */
@Controller
public class HomeController {

    @Autowired
    private DataService dataService;

    @RequestMapping("/")
    public String index(Model model){
        List<Datum> data = dataService.findAll();
        model.addAttribute("allPosts",data);
        return "index";
    }

    @GetMapping("/data")
    public String data(Model model){
        List<Datum> foo = dataService.findAll();
        model.addAttribute("dataEntries",foo);
        List<String> bar = Datum.getFields();
        model.addAttribute("headers",bar);
        model.addAttribute("datum", new Datum());

        return "data";
    }


    @PostMapping("/data")
    public ModelAndView saveDatum(@ModelAttribute Datum datum, BindingResult errors) {

        ModelAndView mv = new ModelAndView("redirect:data");
        if(errors.hasErrors()){
            System.out.println("There was a error "+errors);
        }
        dataService.create(datum);


        System.out.println(datum.getId());
        return mv;
    }
}
