package com.architectlab.web;

import com.architectlab.command.CommandRequest;
import com.architectlab.command.CommandResult;
import com.architectlab.command.CommandService;
import com.architectlab.security.CommandAuthorizationService;
import java.security.Principal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commands")
public class CommandController {
    private final CommandService commandService;
    private final CommandAuthorizationService commandAuthorizationService;

    public CommandController(CommandService commandService, CommandAuthorizationService commandAuthorizationService) {
        this.commandService = commandService;
        this.commandAuthorizationService = commandAuthorizationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public CommandResult execute(@RequestBody CommandRequest request, Authentication authentication, Principal principal) {
        if (!commandAuthorizationService.canExecute(request.type(), authentication)) {
            throw new AccessDeniedException("Command " + request.type() + " requires elevated lab permissions");
        }
        return commandService.execute(request, principal);
    }
}
