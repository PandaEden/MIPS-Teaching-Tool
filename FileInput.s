	.data
x: .word 5
	.text
main:
  # la $t0, x	# Load address of label x to register t0
  lw $s0, 268500992($t0) # Load value at address t0 to s0

  add $s1, $s0, $zero # duplicate the value from s0 to s1
  addi $t9, $s0, -4 # subtract 4 from the value of s0, store in t9

  sub $s1, $s1, $t9 # subtract value of t9 from s1
  addi $s3, $t9, 25 # add 25 to t9 and store in s3
  addi $t3, $zero, 8 # store 8 in t3

  sw $s3, 268500992($t3) # store the value at s3 to address t2

  sw $s3, 0($t2) # store the value at s3 to address t2
halt:
  exit #system call to exit
