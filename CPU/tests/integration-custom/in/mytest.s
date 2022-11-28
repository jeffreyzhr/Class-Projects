li a0 1
addi a0 a0 1
addi a0 a0 1
addi a0 a0 1
addi a0 a0 1
addi a0 a0 1


# I tests
addi t0 x0 10
addi s0 x0 16
slli t1 t0 3
slti t2 t1 10
xori t3 t1 5
srli t1 t1 3
srai t5 s0 3
ori t6 s0 4
andi a0 s0 4

# R tests
li t0 1
li t1 2
add t2, t0, t1
mul t3, t2, t1
sub t4, t3, t2
sll t5, t4, t0
mulh t6, t5, t2

#U Tests
lui a0 1
lui a0 2
lui a0 3

# B TESTS
li s0 2
li a0 1
bge s0 a0 label
label:
    addi a0 a0 -2
    

