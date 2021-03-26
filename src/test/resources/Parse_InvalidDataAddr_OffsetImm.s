	.data
	.word 5

main:	.code

lw	32768($0)	# maximum immedite allowed 2^15-1

# on parse, expect error, Not Valid Data Address
