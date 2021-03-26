x: .word 50

j x

lw $1, y    # Label does not exist

lw $1, 20($1) # Execution Error not caught
