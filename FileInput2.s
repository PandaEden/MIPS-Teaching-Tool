	.text  # https://www.cs.umd.edu/~meesh/cmsc311/clin-cmsc311/Lectures/lecture15/C_code.pdf
	# Need to turn off "Delayed Branching" in MARS for code to work.
	#Code was writing without regard for Branch Delay Slots
main: #comment
	la $s0, size	#init registers
	lw $s1, 0($s0)	# $S1 = SIZE
	ori $s2, $zero, 0 # $s2:sum
	ori $s3, $zero, 0 # $s3:pos
	ori $s4, $zero, 0 # $s4:neg 
	
	# <init>
	ori $s5, $zero, 0 # $s2:sum
	la $s6, arr

	#<for ; if >
L1:	bge $s5, $s1, DONE	# if (i >= arr_SIZE); goto DONE

	#<loop-body>
	lw $s7, 0($s6)		# $s7 = arr[i]
	addu $s2, $s2, $s7	# $sum += arr[i]
	blez $s7, NEG		# if !(arr[i] >0)
	addu $s3, $s3, $s7	#	pos += arr[i];
	j UPDATE		# goto UPDATE

NEG:	bgez $s7, UPDATE	# if !(arr[i]<0); goto UPDATE
	addu $s4, $s4, $s7	# 	neg += arr[i];

UPDATE:	addi $s5, $s5, 1	#i++
	addi $s6, $s6, 4	# increment array pointer
	j L1			# goto L1
DONE:
	#halt
	
	.data #0x10010000
size:	.word 10
arr:	.word 12, -1, 8, 0, 6, 85, -74, 23, 99, -30