package org.hl7.davinci.ehrserver.requestgenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;


@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.hl7.davinci.ehrserver.requestgenerator")
public class RequestGeneratorMvcConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {
    theRegistry.addResourceHandler("/**").addResourceLocations("/WEB-INF/reqgen/");
    theRegistry.addResourceHandler("/static/css/**").addResourceLocations("/WEB-INF/reqgen/static/css/");
    theRegistry.addResourceHandler("/static/js/**").addResourceLocations("/WEB-INF/reqgen/static/js/");
    theRegistry.addResourceHandler("/static/media/**").addResourceLocations("/WEB-INF/reqgen/static/media/");
  }

  /**
   * Setup the template resolver with the location of the templates.
   */
  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
    resolver.setPrefix("/WEB-INF/reqgen/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    return resolver;
  }

  /**
   * Setup the view resolver.
   */
  @Bean
  public ThymeleafViewResolver viewResolver() {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine());
    viewResolver.setCharacterEncoding("UTF-8");
    return viewResolver;
  }

  /**
   * Setup the template engine.
   */
  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(templateResolver());
    return templateEngine;
  }

}
