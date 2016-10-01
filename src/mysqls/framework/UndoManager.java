package mysqls.framework;

import java.util.Stack;

import mysqls.commands.Command;
import mysqls.commands.CompoundCommand;

/**
 * Performs the undoing and redoing of commands on a graph.
 * 
 * @author EJBQ
 *
 */
public class UndoManager {
	private Stack<Command> aPastCommands; // the commands that have been input
											// and can be undone
	private Stack<Command> aUndoneCommands; // the commands that have been
											// undone and can be redone
	private Stack<CompoundCommand> aTrackingCommands; // used for many commands
														// coming at once
	private boolean aHoldChanges = false; // turned on while undoing or redoing
											// to prevent duplication

	/**
	 * Creates a new UndoManager with the GraphPanel. These should be assigned
	 * one per panel.
	 */
	public UndoManager() {
		aPastCommands = new Stack<Command>();
		aUndoneCommands = new Stack<Command>();
		aTrackingCommands = new Stack<CompoundCommand>();
	}

	/**
	 * Adds a command to the stack to be undone. Wipes the redone command if
	 * there is anything there. Will not add the command if changes are being
	 * held, which occurs when we are in the middle of executing a command.
	 * 
	 * @param pCommand
	 *            The command to be added
	 */
	public void add(Command pCommand) {
		if (!aHoldChanges) {
			if (!aUndoneCommands.empty()) {
				aUndoneCommands.clear();
			}
			if (!aTrackingCommands.empty()) {
				aTrackingCommands.peek().add(pCommand);
			} else {
				aPastCommands.push(pCommand);
			}
		}
	}

	/**
	 * Undoes a command. Holds changes so no new commands are added during this.
	 * Adds the command to the redone stack.
	 */
	public void undoCommand() {
		if (aPastCommands.empty()) {
			return;
		}
		aHoldChanges = true;
		Command toUndo = aPastCommands.pop();
		toUndo.undo();
		aUndoneCommands.push(toUndo);
		aHoldChanges = false;
	}

	/**
	 * Pops most recent undone command and executes it. Holds changes so no new
	 * commands are added during this. Adds the command to the redone stack.
	 */
	public void redoCommand() {
		aHoldChanges = true;
		if (aUndoneCommands.empty()) {
			return;
		}
		Command toRedo = aUndoneCommands.pop();
		toRedo.execute();
		aPastCommands.push(toRedo);
		aHoldChanges = false;
	}

	/**
	 * Creates a compound command that all coming commands will be added to.
	 * Used to perform many commands at once
	 */
	public void startTracking() {
		aTrackingCommands.push(new CompoundCommand());
	}

	/**
	 * Finishes off the compound command and adds it to the stack.
	 */
	public void endTracking() {
		if (!aTrackingCommands.empty()) {
			CompoundCommand compoundCommand = aTrackingCommands.pop();
			if (compoundCommand.size() > 0) {
				add(compoundCommand);
			}
		}
	}

}
