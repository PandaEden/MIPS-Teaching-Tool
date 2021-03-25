
					; valid - blank
	.word5				# invalid directive -line3
	.woasd 5,7			# invalid directive	- line4
 .word 5:7,9			; Warning # dataType not specified after ".data" segment directive -line5
						# invalid data - forRange -line5

asdikhbjkbjfkbjsdyu		# invalid opcode -line8
1238989hwedhuwqd7823	# invalid opcode -line9
1line:					# invalid label -line10

and 5 ,5, 6				# Not Supported opcode
add $0, $s0, $5		; valid - Warning # Writing to 0 register -line13

val_label: panda 589	# invalid opcode

.label: j val_label		# invalid label -line17 - will be interpreted as a directive
	__2: j .label		# invalid label_op -line18
__2: j val_label		# label already specified -line18

 _.l3: j val_label:		# invalid label -line19


lw R0 0x10010008		# invalid operands (no comma) -line24
lw R0, _.l3			; Warning # Writing to 0 register -line25
						# invalid Data Address -line25

p a n d a: addi 5, R70, 32768
	# invalid label -line28, 	# invalid operand[0]_format -line28
	# invalid operand[1] out of range -line28
	# invalid operand[2](immediate) out of range -line28

sw $1, ( )				;  assuming R0 ? : invalid data address
SW $1, -32769			# invalid operands(immediate) out of range -line34

j 8589934592		# Not Valid Int- line36

j					# missing operands - line 38
exit r0				# expecting no operands!
