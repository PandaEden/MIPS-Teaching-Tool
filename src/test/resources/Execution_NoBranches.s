	.data		; testing .data directive
	.word 50	# address 0x10010000 /index0

	.text		; testing .text directive
# manually loading address index0
lw $t0, y		# t0/$8 ;forwards declaration, and load from label

lw $s0, (t0)		; testing base+offset, and without offset imm specified.
				# s0/$16 == 50
lw S4,	0x290($8)	; test base+offset, with hex offset (82*8),
; and index_reg_reference,
				# s4/$20 == -900


.data
y:	.word 268500992	# /index1/ Value = address 0x10010000
x:	.word -3:80	# index2->81? label on same line as data range 
	.word -900	# address 0x10010290 /index 82

.code			# testing .code directive, and mixing of text/data sections
addi R8, $zero, 16	# offset the register $t0/$8, to point to x:
sw   $S4, 0 (T0)	# set index2 == -900
add $22, $t0, zero	# s6/$22 == -900
SUB  $24   ,  $s4, $s0	# t8/$24 = (-900 - 50) == -950
; opcode in caps, extra spaces between comma&operands
			.text	; random .text - should not do anything
add $18, $16, $16	# s2/$18 == 100 

; 8 cycles upto here


J del	# testing branch delay slot.

sub $25, $22, $s0	# t9/$25 = -1000 if branch delay slot
add $23, $s0, $s0	# Should not run! s7/$23 = 100 ### means Jump has failed.

del: add $s4, $18, $22	# s4/$20 == -800

; + 2 cycles, or +3 if branch delay
; +1 auto Exit.

; = 11 cycles - instant jump
; = 12 cycles - branch delay


; run off end - warning , auto exit.



# check registers
	# t0/$8  == "0x100100000"/ 268500992
	; .... == 0
	# s0/$16 == 50
	; s2/$18 == 100
	# s4/$20 == -800
	# s6/$22 == -900
	# s7/$23 == 0 ; NOT == 100
	# t8/$24 == -950

	# if BranchDelaySlot
		# Check t9/$25 == -1000
	# else
		# Check t9/$25 == 0

# check memory
	# index 0 == 50
	# index 1 == "0x10010000"
	# index 2 == -900
	# index 3 == -3
		;......
	# index 82 == -3
	# index 83 == -900
	# index 84 == 0
