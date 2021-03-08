; auto .text directive assumed.

j main	; 1 cycles

__1:	; 2 cycles   , 3 with nop / branch delay slot
addi $8, $zero, 100	; t0/$8 +=100
jr
add $1, $zero, $zero	# effective nop	; +1 if branch delay

__2:	; 18, or 23 cycles with branch delay slot
sw $31, x	# store RA ; 1 cycle

jal __1	# 100		; 1 cycle + 2/3 cycles == 3 cycles, or 4 if branch delay
add $1, $zero, $zero	# effective nop ; +1

jal __1	# 200				; +3 /4
add $1, $zero, $zero	# effective nop ; +1

jal __1	# 30				; +3/4
add $1, $zero, $zero	# effective nop ; +1

jal __1	# 400				; +3/4
add $1, $zero, $zero	# effective nop	; +1

; === 16, or 20 cycles if branch delay

lw $31, x	# restore RA, ; 1 cycle

jr		; 1 cycle
add $1, $zero, $zero	# effective nop	; +1 if branch delay


.data
x: .word 0 #reserved to store address


main:
jal __2 ; 1 cycle
add $1, $zero, $zero	# effective nop	; +1

halt ; 1 cycle


; instant jumps : =22 cycles

; branch delay : = 26 cycles
