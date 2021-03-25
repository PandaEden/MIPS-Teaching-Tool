	.data			; testing .data directive
	.word 50		# address 0x10010000 /index0

	.text			; testing .text directive
lw $t0, y			# manually loading address index0
					# t0/$8 ;forwards declaration, and load from label
lw $s0, (t0)		; testing base+offset, and without offset imm specified.
					# s0/$16 == 50
lw S4,	0x290($8)	; test base+offset, with hex offset (82*8),
					; and index_reg_reference,
					# s4/$20 == -900
.data
y:	.word 268500992		# /index1/ Value = address 0x10010000
x:	.word -3:80			# index2->81? label on same line as data range
	.word -900			# address 0x10010290 /index 82

.code						# testing .code directive, and mixing of text/data sections
addi R10, $T0, 16			# offset the register $t2/$10, to point to x:
sw   $S4, 0 (T2)			# set index2 == -900  $s4/$20
add $22, $20, zero			# s6/$22 == -900
SUB  $24   ,  $s4, $s0		# t8/$24 = (-900 - 50) == -950
; opcode in caps, extra spaces between comma&operands
			.text			; random .text - should not do anything
add $18, $16, r16			# s2/$18 == 100
							; 8 cycles up to here
J del	# testing branch delay slot.

sub $25, $22, $s0		# t9/$25 = -1000 if branch delay slot
add $23, $s0, $s0		# Should not run! s7/$23 = 100 ### means Jump has failed.

del: add $s4, $18, $22		# s4/$20 == -800

; + 2 cycles, or +3 if branch delay
; +1 auto Exit.

; = 11 cycles - instant jump
; = 12 cycles - branch delay

; run off end - warning , auto exit. Exit_PC = 0x0040030

# check registers
	# at/$1 might be set to "0x100100000"/ 268500992
		# if pseudo instructions are properly decoded
	# t0/$8  == "0x100100000"/ 268500992
	# t2/$10 == "0x100100010"/ 268501008
	  ; ...  == 0
	# s0/$16 == 50
	; s2/$18 == 100
	# s4/$20 == -800
	# s6/$22 == -900
	# s7/$23 == 0 	; if == 100, there is a fault with the jump
	# t8/$24 == -950

	# if BranchDelaySlot
		# Check t9/$25 == -1000
	# else
		# Check t9/$25 == 0

# check memory
	# 0x10010000:: index 0	== 50
	# 0x10010008:: index 1	== "0x10010000"/ 268500992
	# 0x10010010:: index 2	== -900
	# 0x10010018:: index 3	== -3
		; ... == -3
	# 0x10010288:: index 81 == -3
	# 0x10010290:: index 82 == -900
	# 0x10010298:: index 83 == 0
		; ... == 0
	# 0x10010298:: index 255 == 0
