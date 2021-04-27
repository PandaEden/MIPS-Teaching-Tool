
addi $8, $8, 50
addi $9, $9, 40

start:

addi $10, $10, 1 # counter
addi $9, $9, 1

bgt $8, $9, start


## Done.
  # Counter should be 10
  # $8 == 40
  # $9 == 40
