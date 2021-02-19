# Arbitrary limits

> These may change in future releases after doing more testing.

`MAX_FILE_LENGTH = 500` (Lines)
`MAX_INS_COUNT = 250` (Instructions)  *upto 251 instructions including auto Exit.
`MAX_DATA_SEGMENTS = 200`	(arbitraty limit for testing purposes)

MEMORY DATA Segments are DoubleWord addressable (multiple of 8) to facilitate and simplify future double-precision float support.
	This makes the last addressable block of memory ()

WORDS are all presumed to be signed (32bit) integers. This is to match Java's built in Integer data type.

Support for more data types (single-precision float, doubleWords, half-words, bytes) could be implemented in the future. But complete support of the MIPS ISA is not main focus of the application.

# Standard use:

	All input is made lowercase when parsed. This means ($ZERO, $Zero, $zero) are all treated the same.

## Supported Instructions:

> Format: **[OpCode]**\[Whitespace\]**[Operands]**

- **ADD**	`Operands_R`
- **SUB**	`Operands_R`
- **ADDI**	`Operands_I1`
- **LW** 	`Operands_I3` # (pseudo)
- **SW**	`Operands_I3` # (pseudo)


- **J** 	`Direct_Address`
- **JAL** 	`Direct_Address` # Register $ra is overwritten
- **HALT**	# No Operands
- **EXIT**	# No Operands - same as HALT

**Pseudo instructions are not accurately executed**

	- they do not assemble into multiple instructions, /Take multiple cycles to execute
	- make use of the $at register,

## Operands Format:

<,\s*> - mean comma ',' followed by 0 or more whitespace '\s*'.

 @See **Supported Address Segments** [TODO:link]

| Operands Type:     | Operand 1                  | <,\s*> | Operand 2             | <,\s*> | Operand 3               |
| ------------------ | -------------------------- | ------ | --------------------- | ------ | ----------------------- |
| **Operands_R:**    | **[Destination_Register]** |        | **[Source_Register]** |        | **[Third_Register**     |
| **Operands_I1**:   | **[Third_Register]**       |        | **[Source_Register]** |        | [Immediate]             |
| **Operands_I2:**   | **[Third_Register]**       |        | [Offset]              | N/A    | (**[Source_Register]**) |
| **Operands_I3:**   | **[Third_Register]**       |        | [Immediate]           |        |                         |
| **Direct_Address** | [Address]                  |        |                       |        |                         |

## Operands:

> [Destination_Register]/[Source_Register]/[Third_Register]

 - _[Third_Register]_ $rt can be the Destination or Source register for I_type instructions._

 - [Immediate] Values must be valid signed 16bit integers.

 - [Offset] is an [Immediate] value used for Memory addressing.

	- Offsets used in branches are multiplied by 4, to force them to align with instruction addresses.

	- This is not done for load/store, as in other implementations memory is byte addressable.

		- It is upto the user to ensure offsets used with Load/Store are doubleWord (8bytes) addressable.

		- Meaning the Offset+$RD_Val is a multiple of 8.

 - [Address] are unsigned 28bit integers.

_Labels are Strings, which reference an address. - converted into fixed values at assembly.

### Registers

| Supported | Number | Name  | Purpose                                       | Preserved*[2] |
| --------- | :----: | :---: | --------------------------------------------- | ------------- |
| Yes* [0]  |   0    | ZERO  | Always equal to zero;                         | *N/A*         |
| NO *[1]   |   1    |  AT   | Assembler temporary; used by the assembler    | NO            |
| Yes *[2]  |  2-3   | V0-V1 | Return value from a function call             | NO            |
| Yes *[2]  |  4-7   | A0-A3 | First four parameters for a function call     | NO            |
| Yes       |  8-15  | T0-T7 | Temporary variables;                          | NO            |
| Yes       | 16-23  | S0-S7 | Function variables;                           | **YES**       |
| Yes       | 24-25  | T8-T9 | Two more temporary variables                  | NO            |
| NO        | 26-27  | K0-K1 | Kernel use registers; may change unexpectedly | NO            |
| NO        |   28   |  GP   | Global pointer                                | **YES**       |
| NO *[2]   |   29   |  SP   | Stack pointer                                 | **YES**       |
| NO *[3]   |   30   | FP/S8 | Stack frame pointer or subroutine variable    | **YES**       |
| Yes *[2]  |   31   |  RA   | Return address of the last subroutine call    | **YES**       |

 - [0] Using this register as a destination register, is effectively a "nop". - Warnings are issued in parsing phase.
 - [1] Used by assembler to recode pseudo instructions into actual ones. Avoid using yourself.
    - pseudo instructions not currently broken down - hence not used by assembler in current build.
 - [2] Function Calls not tested/ Implemented, Therefor StackPointer disabled until this is tested.
 - [3] FramePointer not implemented, reference this register as $s8 if used named referencing.

Registers can be referenced by name (e.g. $s2, $t0, $zero) or R_Number (e.g. $r18, $r8).

'$' is required.

## Labels

Labels must start with a letter (a-z/A-Z) (The application is case-insensitive)

 - periods,hyphens and underscores '.','-','_' may be used in a label name for readability.

 - no other symbols can be used, and spaces are not allowed. 

 - Label references are parsed for errors after instructions are parsed.

In the place of a label operand for an instruction a Hexadecimal address may be written instead, or the address as a decimal number.

 - Hexadecimal values must start with "0x".

### Supported Address Segments

Labels are converted into addresses at assembly after all the code has been parsed.
Jump and Branch instructions can reference Labels directed to instruction address space.

Load and Store instructions can reference Labels directed to data address space.

`Code segments 0x00400000 to 0x00500000 in steps of 4, 2^18 valid segments`

`Data segments 0x10010000 to 0x100107F8 in steps of 8, 2^8 valid segments`

Only 250 instructions supported, (which is a maximum address of 0x004003E8).
Jumping to this gap, which is a valid instruction address, where there are no instructions to execute.
An Exit ('halt') instruction will automatically be ran next.

###### MIPS Register addressing

	Allows a full 32bit address to be loaded into a register.
	Then jump instructions / Load&Store use the address stored in the register.
	
	This introduced a runtime address hazard not detectable by the parser.
	The value of the register is determined at runtime.
	In the Execution Phase the address read from the instruction can be checked to be valid.

###### MIPS Base addressing - Base+Offset

MIPS Base addressing - Base+Offset

I_Type

- Offset(base) - Valid Data Address - Offset Addressing.
	- (later builds may introduce pseudo instructions for offset(label))

- Label
    - 	Branch - Converted to Offset.
    - 	Load/Store - Pseudo Instruction (will be adjusted in future build)

J_Type

- Address - Valid Code Address - Direct Addressing.
- Label directed to a code address. e.g. "main",

# Error/Warning messages:

## Valid File Checks:
	Check - File Exists
	Check - File is accessible (not being used by another resource)
	Check - File Length
	
	If any of these fails, the application will terminate.
		(future versions may allow selecting a new file).

## Parsing:
> Whitespace is trimmed, and case is converted to lowercase.
> Parser checks file contains no syntactical errors.

	From this the Parser builds a model:
	Directive					(.data, .text, .code)
		\
		Comments				(comments can begin with a pound sigh '#', or semicolon ';')
		   \					(comments are not used in any way by the application)
			\					
			Labels				(Labels must end with a colon ':')
				\
				Sub_Directive	(.word //future support for .double planned)
				|	\
				|	Values		(single int, int range, int_array)
				|	
				OpCode			(see list of supported instructions)
					\
					Opperands	(depends on instruction type^)
						\
						Extra
				- Any extra characters past the last opperand, but not in the comments section.
				- A Warning will be issued, but these characters will be ignored.
	
	Over 250 Instructions are read:
		A Warning will be issued And no further instructions will be parsed.
		
	If no EXIT/'halt' instruction is read:
		A warning will be issued. And one will automatically be appended to the end.

Parser will not stop after an error is thrown. It will attempt to parse the remainder of the lines checking for additional errors. However - It will not allow execution.

Since the labels can point to code/ data not yet parsed, The parser caches the instructions using labels.

Then runs a 2nd time converting the labels into addresses/offsets.


# MIPS Addresses Segments

```
*  Code range (.text) 0x00400000 to 0x004FFFFF (4194304 to 5242879)
*  
*  Global Data (.data) 0x10010000 to 0x1003FFFF (268500992 to 268697599)
*  Heap data 0x10040000 to 0x1FFFFFFF (268697600 to 536870911)
*  Stack data 0x70000000 to 0x7FFFFFFF (1879048192 to 2147483647)
* 
*  In powers:
*      0x00400000:(2^22)       >= Code     <0x00500000:(2^22 +2^20)
*      0x10010000:(2^28 +2^16) >= Global   <0x10040000:(2^28 +2^18)
*      0x10040000:(2^28 +2^18) >= Heap     <0x20000000:(2^29)
*      0x70000000:(2^31-2^28)  >= Stack    <0x80000000:(2^31)
```

.data segment usually has a size of 49152 word address (2^15+2^14). Addresses 0x10010000 to 0x1003FFFC.
	As double words this becomes 24576 doubleword address (2^14+2^13). Addresses 0x10010000 to 0x1003FFF8.

_Hex notation 0xXXXXXXXX (8 digits = 32bit Address)_