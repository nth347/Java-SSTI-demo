package nth347.javasstidemo;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@Controller
public class MainController {
    @RequestMapping("/ssti/thymeleaf")
    @ResponseBody
    public String thymeleaf(@RequestParam(defaultValue="nth347") String username, HttpServletRequest request, HttpServletResponse response) {
        String templateString = "Hello, " + username + " | Full name: [[${name}]], phone: [[${phone}]], email: [[${email}]]";

        TemplateEngine templateEngine = new SpringTemplateEngine(); // Use SpringTemplateEngine() for SpEL
        ITemplateResolver templateResolver = new StringTemplateResolver();
        templateEngine.setTemplateResolver(templateResolver);

        WebContext ctx = new WebContext(request, response, request.getServletContext());
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("name", "Nguyen Nguyen Nguyen");
        variables.put("phone", "012345678");
        variables.put("email", "nguyen@vietnam.com");
        ctx.setVariables(variables);

        Writer out = new StringWriter();
        templateEngine.process(templateString, ctx, out);

        return out.toString();
    }

    @RequestMapping("/ssti/freemarker")
    @ResponseBody
    public String freeMarker(@RequestParam(defaultValue="nth347") String username) {
        String templateString = "Hello, " + username + " | Full name: ${name}, phone: ${phone}, email: ${email}";
        Writer out = new StringWriter();

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("name", "Nguyen Nguyen Nguyen");
        variables.put("phone", "012345678");
        variables.put("email", "nguyen@vietnam.com");

        try {
            // Template name is null because we are using template as a String
            // Configuration is null because we don't need it
            Template templateEngine = new Template(null, new StringReader(templateString), null);
            templateEngine.process(variables, out);
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }

        return  out.toString();
    }

    @RequestMapping("/ssti/velocity1")
    @ResponseBody
    public String velocity1(@RequestParam(defaultValue="nth347") String username) {
        String templateString = "Hello, " + username + " | Full name: $name, phone: $phone, email: $email";

        Velocity.init();
        VelocityContext ctx = new VelocityContext();
        ctx.put("name", "Nguyen Nguyen Nguyen");
        ctx.put("phone", "012345678");
        ctx.put("email", "nguyen@vietnam.com");

        StringWriter out = new StringWriter();
        Velocity.evaluate(ctx, out, "test", templateString);

        return out.toString();
    }

    @RequestMapping("/ssti/velocity2")
    @ResponseBody
    public String velocity2(@RequestParam(defaultValue="nth347") String username) throws IOException, ParseException {
        String templateString = new String(Files.readAllBytes(Paths.get("template.vm")));
        templateString = templateString.replace("<USERNAME>", username);

        StringReader reader = new StringReader(templateString);

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", "Nguyen Nguyen Nguyen");
        ctx.put("phone", "012345678");
        ctx.put("email", "nguyen@vietnam.com");

        StringWriter out = new StringWriter();
        org.apache.velocity.Template template = new org.apache.velocity.Template();

        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        SimpleNode node = runtimeServices.parse(reader, template);

        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();

        template.merge(ctx, out);

        return out.toString();
    }
}
