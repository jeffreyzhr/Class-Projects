.globl relu

.text
# ==============================================================================
# FUNCTION: Performs an inplace element-wise ReLU on an array of ints
# Arguments:
#   a0 (int*) is the pointer to the array
#   a1 (int)  is the # of elements in the array
# Returns:
#   None
# Exceptions:
#   - If the length of the array is less than 1,
#     this function terminates the program with error code 36
# ==============================================================================
relu:
    # Prologue
    li t1 0 #t1 = 0, this is the index jumper thingy
    li t6 0
    
    # error catching
    li t0 1
    blt a1 t0 error
    

loop_start:
    lw t2 0(a0) # t2 = a[i]
    bge t2 t6 loop_continue # if t2 larger than 0, continue


    sw t6 0(a0) # change array element t1 to 0



loop_continue:
    addi t1 t1 1 # count += 1
    addi a0 a0 4 # &A[i+1]
    bge t1 a1 loop_end
    j loop_start

    
error:
    li a0 36
    j exit

loop_end:



    jr ra

