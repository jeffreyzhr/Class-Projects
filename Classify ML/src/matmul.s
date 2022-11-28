.globl matmul

.text
# =======================================================
# FUNCTION: Matrix Multiplication of 2 integer matrices
#   d = matmul(m0, m1)
# Arguments:
#   a0 (int*)  is the pointer to the start of m0
#   a1 (int)   is the # of rows (height) of m0
#   a2 (int)   is the # of columns (width) of m0
#   a3 (int*)  is the pointer to the start of m1
#   a4 (int)   is the # of rows (height) of m1
#   a5 (int)   is the # of columns (width) of m1
#   a6 (int*)  is the pointer to the the start of d
# Returns:
#   None (void), sets d = matmul(m0, m1)
# Exceptions:
#   Make sure to check in top to bottom order!
#   - If the dimensions of m0 do not make sense,
#     this function terminates the program with exit code 38
#   - If the dimensions of m1 do not make sense,
#     this function terminates the program with exit code 38
#   - If the dimensions of m0 and m1 don't match,
#     this function terminates the program with exit code 38
# =======================================================
matmul:

    # Error checks
    li t6 1
    blt a1 t6 error
    blt a2 t6 error
    blt a4 t6 error
    blt a5 t6 error
    bne a2 a4 error


    # Prologue
    addi sp sp -36
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
    
    mv s0 a6 # s0 = results array
    mv s1 a0 # ptr to A
    mv s2 a1 # rows A
    mv s3 a2 # cols A
    mv s4 a3 #ptr to B
    mv s5 a3 # b ptr moving 
    mv s6 a5 # cols B
    li s7 0


outer_loop_start:
# runs through rows of A
    bge s7 s2 outer_loop_end
    li s8 0
    mv s5 s4
    

inner_loop_start:
# runs through cols of B
    bge s8 s6 inner_loop_end
    
    mv a0 s1
    mv a1 s5 # ptr to B assigned to a1
    mv a2 s3
    li a3 1
    mv a4 s6
    
    
    jal dot
    sw a0 0(s0)
    
    addi s8 s8 1
    addi s5 s5 4
    addi s0 s0 4
    
    j inner_loop_start


inner_loop_end:
    addi s7 s7 1
    
    li t0 4
    
    mul t1 s3 t0
    add s1 s1 t1 #updating ptr A
    
    j outer_loop_start

outer_loop_end:

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
    addi sp sp 36


    jr ra


error:
    li a0 38
    j exit
