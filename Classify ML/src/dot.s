.globl dot

.text
# =======================================================
# FUNCTION: Dot product of 2 int arrays
# Arguments:
#   a0 (int*) is the pointer to the start of arr0
#   a1 (int*) is the pointer to the start of arr1
#   a2 (int)  is the number of elements to use
#   a3 (int)  is the stride of arr0
#   a4 (int)  is the stride of arr1
# Returns:
#   a0 (int)  is the dot product of arr0 and arr1
# Exceptions:
#   - If the length of the array is less than 1,
#     this function terminates the program with error code 36
#   - If the stride of either array is less than 1,
#     this function terminates the program with error code 37
# =======================================================
dot:

    # error catch
    li t0 1
    blt a2 t0 error1
    blt a3 t0 error2
    blt a4 t0 error2
    
    li t0 4 # counter
    li t6 0 # sum
    
    
    

loop_start:
    bge zero a2 loop_end  
    lw t2 0(a0) # a[0]
    lw t3 0(a1) # b[0]
    
    mul t1 t2 t3 # dot of two elements
    
    mul t4 t0 a3 #mem stride of a
    mul t5 t0 a4 #mem stride of b
    
    add a0 a0 t4
    add a1 a1 t5
    
    add t6 t6 t1 # sum += (dot product)

    addi a2 a2 -1
    j loop_start


loop_end:

    mv a0 t6
    jr ra
   
error1:
    li a0 36
    j exit
    
error2:
    li a0 37
    j exit
