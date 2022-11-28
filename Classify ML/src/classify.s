.globl classify

.text
# =====================================
# COMMAND LINE ARGUMENTS
# =====================================
# Args:
#   a0 (int)        argc
#   a1 (char**)     argv
#   a1[1] (char*)   pointer to the filepath string of m0
#   a1[2] (char*)   pointer to the filepath string of m1
#   a1[3] (char*)   pointer to the filepath string of input matrix
#   a1[4] (char*)   pointer to the filepath string of output file
#   a2 (int)        silent mode, if this is 1, you should not print
#                   anything. Otherwise, you should print the
#                   classification and a newline.
# Returns:
#   a0 (int)        Classification
# Exceptions:
#   - If there are an incorrect number of command line args,
#     this function terminates the program with exit code 31
#   - If malloc fails, this function terminates the program with exit code 26
#
# Usage:
#   main.s <M0_PATH> <M1_PATH> <INPUT_PATH> <OUTPUT_PATH>
classify:
    li t0 5
    bne a0 t0 error_arguments
    
    addi sp sp -56
    sw ra 0(sp)
    sw s0 4(sp)
    sw s1 8(sp)
    sw s2 12(sp)
    sw s3 16(sp)
    sw s4 20(sp)
    sw s5 24(sp)
    sw s6 28(sp)
    sw s7 32(sp)
    sw s8 36(sp)
    sw s9 40(sp)
    sw s10 44(sp)
    sw s11 48(sp)
    sw a2 52(sp)
    
    mv s3 a1
    # Read pretrained m0
    li a0 4
    jal malloc 
    beqz a0 malloc_error
    mv s4 a0 # row m0
    
    li a0 4
    jal malloc 
    beqz a0 malloc_error
    mv s5 a0 # col m0
    
    addi t0 s3 4
    lw a0 0(t0)
    mv a1 s4
    mv a2 s5
    
    jal read_matrix
    
    mv s0 a0


    # Read pretrained m1
    li a0 4
    jal malloc 
    beqz a0 malloc_error
    mv s6 a0 # row m1
    
    li a0 4
    jal malloc 
    beqz a0 malloc_error
    mv s7 a0 # col m1
    
    addi t0 s3 8
    lw a0 0(t0)
    mv a1 s6
    mv a2 s7
    
    jal read_matrix
    
    mv s1 a0


    # Read input matrix
    li a0 4
    jal malloc 
    beqz a0 malloc_error
    mv s8 a0 # input row
    
    li a0 4
    jal malloc 
    beqz a0 malloc_error
    mv s9 a0 # input col
    
    addi t0 s3 12
    lw a0 0(t0)
    mv a1 s8
    mv a2 s9
    
    jal read_matrix

    mv s2 a0

    # Compute h = matmul(m0, input)
    lw t0 0(s4) # num rows in m0
    lw t1 0(s9) # num cols in input
    mul a0 t0 t1
    li t2 4
    mul a0 a0 t2
    jal malloc
    beqz a0 malloc_error
    mv a6 a0
    mv s10 a0
    mv a0 s0
    lw a1 0(s4)
    lw a2 0(s5)
    mv a3 s2
    lw a4 0(s8)
    lw a5 0(s9)
    
    jal matmul
    
    # Compute h = relu(h)
    mv a0 s10
    lw t0 0(s4)
    lw t1 0(s9)
    mul a1 t0 t1
    
    jal relu

    # Compute o = matmul(m1, h)
    lw t0 0(s6) # num rows in m1
    lw t1 0(s9) # num cols in h
    mul a0 t0 t1
    li t2 4
    mul a0 a0 t2
    jal malloc
    beqz a0 malloc_error
    
    mv a6 a0
    mv s11 a0 # saving output of matmul into s11
    mv a0 s1
    lw a1 0(s6)
    lw a2 0(s7)
    mv a3 s10
    lw a4 0(s4)
    lw a5 0(s9)
    
    jal matmul

    # Write output matrix o
    addi t0 s3 16
    lw a0 0(t0)
    mv a1 s11
    lw a2 0(s6)
    lw a3 0(s9)
    jal write_matrix

    # Compute and return argmax(o)
    mv a0 s11
    lw t0 0(s6) # num rows in o
    lw t1 0(s9) # num cols in o
    mul a1 t0 t1
    
    jal argmax
    
    mv s3 a0
    # If enabled, print argmax(o) and newline
    lw a2 52(sp)
    beqz a2 print
    j continue
    
print:
    mv a0 s3
    jal print_int
    li a0 '\n'
    jal print_char
    
continue:
    mv a0 s0
    jal free
    mv a0 s1
    jal free
    mv a0 s2
    jal free
    
    mv a0 s4
    jal free
    mv a0 s5
    jal free
    mv a0 s6
    jal free
    mv a0 s7
    jal free
    mv a0 s8
    jal free
    mv a0 s9
    jal free
    mv a0 s10
    jal free
    mv a0 s11
    jal free

    mv a0 s3
    ebreak
    lw ra 0(sp)
    lw s0 4(sp)
    lw s1 8(sp)
    lw s2 12(sp)
    lw s3 16(sp)
    lw s4 20(sp)
    lw s5 24(sp)
    lw s6 28(sp)
    lw s7 32(sp)
    lw s8 36(sp)
    lw s9 40(sp)
    lw s10 44(sp)
    lw s11 48(sp)
    lw a2 52(sp)
    addi sp sp 56
    
    jr ra

error_arguments:
    li a0 31
    j exit
malloc_error:
    li a0 26
    j exit