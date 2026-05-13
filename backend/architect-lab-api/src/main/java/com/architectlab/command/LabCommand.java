package com.architectlab.command;

public interface LabCommand {
    CommandType commandName();

    CommandResult execute(CommandContext context, CommandRequest request);
}
