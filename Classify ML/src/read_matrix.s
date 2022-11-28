.globl read_matrix

.text
# ==============================================================================
# FUNCTION: Allocates memory and reads in a binary file as a matrix of integers
#
# FILE FORMAT:
#   The first 8 bytes are two 4 byte ints representing the # of rows and columns
#   in the matrix. Every 4 bytes afterwards is an element of the matrix in
#   row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is a pointer to an integer, we will set it to the number of rows
#   a2 (int*)  is a pointer to an integer, we will set it to the number of columns
# Returns:
#   a0 (int*)  is the pointer to the matrix in memory
# Exceptions:
#   - If malloc returns an error,
#     this function terminates the program with error code 26
#   - If you receive an fopen error or eof,
#     this function terminates the program with error code 27
#   - If you receive an fclose error or eof,
#     this function terminates the program with error code 28
#   - If you receive an fread error or eof,
#     this function terminates the program with error code 29
# ==============================================================================
read_matrix:

    # Prologue
    addi sp sp -28
    sw ra 0(sp)
    sw s0 4(sp)
    sw s1 8(sp)
    sw s2 12(sp)
    sw s3 16(sp)
    sw s4 20(sp)
    sw s5 24(sp)
    
    mv s0 a0
    mv s1 a1
    mv s2 a2
    
    
    #start of fopen call
    li a1 0
    
    jal fopen
    
    li t0 -1
    beq a0 t0 error1
    mv s0 a0
    #end of fopen call
    
    #start of fread rows
    mv a0 s0
    mv a1 s1
    li a2 4
    
    jal fread
    
    li t0 4
    bne a0 t0 error2
    lw s3 0(s1)
    # end of fread rows
    
    #start of fread cols
    mv a0 s0
    mv a1 s2
    li a2 4
    
    jal fread
    
    li t0 4
    bne a0 t0 error2
    lw s4 0(s2)
    # end of fread cols
    
    #Start of malloc
    mul a0 s3 s4
    li t0 4
    mul a0 a0 t0 
    
    jal malloc
    
    beqz a0 error3
    mv s5 a0
    #end of malloc
    
    #start fread matrix
    mv a0 s0
    mv a1 s5
    mul a2 s3 s4
    li t0 4
    mul a2 a2 t0
    
    jal fread
    
    mul t0 s3 s4
    li t1 4
    mul t0 t0 t1
    bne a0 t0 error2
    # end fread matrix
    
    #start fclose
    mv a0 s0 
    jal fclose
    bnez a0 error4
    #end fclose

    # Epilogue
    mv a0 s5

    lw ra 0(sp)
    lw s0 4(sp)
    lw s1 8(sp)
    lw s2 12(sp)
    lw s3 16(sp)
    lw s4 20(sp)
    lw s5 24(sp)
    addi sp sp 28

    jr ra
    
error1:
    li a0 27
    j exit
error2:
    li a0 29
    j exit
error3:
    li a0 26
    j exit
error4:
    li a0 28 
    j exit
