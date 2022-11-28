.globl argmax

.text
# =================================================================
# FUNCTION: Given a int array, return the index of the largest
#   element. If there are multiple, return the one
#   with the smallest index.
# Arguments:
#   a0 (int*) is the pointer to the start of the array
#   a1 (int)  is the # of elements in the array
# Returns:
#   a0 (int)  is the first index of the largest element
# Exceptions:
#   - If the length of the array is less than 1,
#     this function terminates the program with error code 36
# =================================================================
argmax:
    li t0 0  #counter = 0
    
    # error catch
    li t6 1
    blt a1 t6 error
    
    lw t1 0(a0) # t1 = a[0]
    mv t2 t1 # comparator t2
    li t3 0 # return value
    
loop_start:
    blt t1 t2 loop_continue
    mv t3 t0
    mv t2 t1

loop_continue:
    addi t0 t0 1
    addi a0 a0 4
    bge t0 a1 loop_end
    lw t1 0(a0) # t1 = a[i]
    j loop_start


loop_end:
    # Epilogue
    mv a0 t3
    jr ra
    
error:
    li a0 36
    j exit
