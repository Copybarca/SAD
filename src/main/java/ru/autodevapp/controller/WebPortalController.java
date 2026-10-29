package ru.autodevapp.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.autodevapp.exception.ResourceNotFoundException;
import ru.autodevapp.model.entity.TaskStatus;
import ru.autodevapp.model.web.service.WebPortalService;
import ru.autodevapp.view.web.ScrumItemForm;
import ru.autodevapp.view.web.WebPortalViewMapper;

/**
 * Обслуживает серверные HTML-представления внутреннего web-портала.
 */
@Controller
public class WebPortalController {

    private final WebPortalService service;
    private final WebPortalViewMapper mapper;

    public WebPortalController(WebPortalService service, WebPortalViewMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @ModelAttribute("scrumForm")
    public ScrumItemForm scrumForm() {
        return new ScrumItemForm();
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/ui";
    }

    @GetMapping("/ui")
    public String dashboard(Model model) {
        model.addAttribute("page", mapper.toDashboardPage(service.loadDashboard()));
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/ui/tasks")
    public String tasks(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) TaskStatus status,
            Model model) {
        model.addAttribute("page", mapper.toTaskListPage(
                service.findTasks(module, status),
                service.findTasks(null, null),
                module,
                status));
        model.addAttribute("activePage", "tasks");
        return "tasks/list";
    }

    @GetMapping("/ui/tasks/{taskId}")
    public String taskDetails(@PathVariable Long taskId, Model model) {
        model.addAttribute("page", mapper.toTaskDetailsPage(service.findTask(taskId)));
        model.addAttribute("activePage", "tasks");
        return "tasks/details";
    }

    @GetMapping("/ui/scrum")
    public String scrum(@RequestParam(required = false) String module, Model model) {
        addScrumPage(model, module);
        return "scrum/index";
    }

    @PostMapping("/ui/scrum")
    public String createScrumItem(
            @Valid @ModelAttribute("scrumForm") ScrumItemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addScrumPage(model, null);
            return "scrum/index";
        }
        service.createScrumItem(form.getTitle(), form.getDescription(), form.getModule());
        redirectAttributes.addFlashAttribute("successMessage", "Scrum-задача добавлена");
        return "redirect:/ui/scrum";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(ResourceNotFoundException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        model.addAttribute("activePage", "");
        return "error/not-found";
    }

    private void addScrumPage(Model model, String module) {
        model.addAttribute("page", mapper.toScrumPage(
                service.findScrumItems(module),
                service.findScrumItems(null),
                module));
        model.addAttribute("activePage", "scrum");
    }
}
