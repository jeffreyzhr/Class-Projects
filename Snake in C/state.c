#include "state.h"

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "snake_utils.h"

/* Helper function definitions */
static void set_board_at(game_state_t* state, unsigned int row, unsigned int col, char ch);
static bool is_tail(char c);
static bool is_head(char c);
static bool is_snake(char c);
static char body_to_tail(char c);
static char head_to_body(char c);
static unsigned int get_next_row(unsigned int cur_row, char c);
static unsigned int get_next_col(unsigned int cur_col, char c);
static void find_head(game_state_t* state, unsigned int snum);
static char next_square(game_state_t* state, unsigned int snum);
static void update_tail(game_state_t* state, unsigned int snum);
static void update_head(game_state_t* state, unsigned int snum);

/* Task 1 */
game_state_t* create_default_state() {
  
  game_state_t *default_state;
  default_state = (game_state_t *) malloc(sizeof(game_state_t));
  if (!default_state) {return NULL;}

  unsigned int nr = 18;
  unsigned int ns = 1;

  default_state->num_rows = nr;
  default_state->num_snakes = ns;
  default_state->board = malloc((nr + 1) * sizeof(char*));
  default_state->snakes = malloc(ns*sizeof(snake_t));
  if (!default_state->board) {return NULL;}
  if (!default_state->snakes) {return NULL;}

  //intialise **boards
  char *n = (char * ) 0;
  default_state->board[nr] = n;
  unsigned int row_len = 20;
  char wall_str[] = "####################";
  char board_str[] = "#                  #";
  char fruit_str[] = "# d>D    *         #";

  for (int i = 0; i < nr; i++) {
    default_state->board[i] = malloc((row_len + 1) * sizeof(char));
    if (i == 0 || i == 17){
      strcpy(default_state->board[i], wall_str);
      continue;
    } 
    if (i == 2){
      strcpy(default_state->board[i], fruit_str);
      continue;
    }
    strcpy(default_state->board[i], board_str);
  }

  //intialise snakes
  //default_state->snakes = (snake_t *) malloc(ns*sizeof(snake_t));

  snake_t solo;

  solo.head_col= 4;
  solo.head_row = 2;
  solo.tail_col = 2;
  solo.tail_row = 2;
  solo.live = true;

  default_state->snakes[0] = solo;

  return default_state;
}

/* Task 2 */
void free_state(game_state_t* state) {
  for (int i = 0; i < state->num_rows; i++){ 
    free(state->board[i]);
  }
  free(state->board);
  free(state->snakes);
  free(state);
  return;
}

/* Task 3 */
void print_board(game_state_t* state, FILE* fp) {
  for (int i = 0; i < state->num_rows; i++){
    fprintf(fp,"%s\n", state->board[i]);
  }
  return;
}

/*
  Saves the current state into filename. Does not modify the state object.
  (already implemented for you).
*/
void save_board(game_state_t* state, char* filename) {
  FILE* f = fopen(filename, "w");
  print_board(state, f);
  fclose(f);
}

/* Task 4.1 */

/*
  Helper function to get a character from the board
  (already implemented for you).
*/
char get_board_at(game_state_t* state, unsigned int row, unsigned int col) {
  return state->board[row][col];
}

/*
  Helper function to set a character on the board
  (already implemented for you).
*/
static void set_board_at(game_state_t* state, unsigned int row, unsigned int col, char ch) {
  state->board[row][col] = ch;
}

/*
  Returns true if c is part of the snake's tail.
  The snake consists of these characters: "wasd"
  Returns false otherwise.
*/
static bool is_tail(char c) {
  char *p;
  char tail[] = "wasd";
  p = strchr(tail, c);

  return p;
}

/*
  Returns true if c is part of the snake's head.
  The snake consists of these characters: "WASDx"
  Returns false otherwise.
*/
static bool is_head(char c) {
  char *p;
  char tail[] = "WASDx";
  p = strchr(tail, c);

  return p;
}

/*
  Returns true if c is part of the snake.
  The snake consists of these characters: "wasd^<v>WASDx"
*/
static bool is_snake(char c) {
  char *p;
  char tail[] = "wasd^<v>WASDx";
  p = strchr(tail, c);

  return p;
}

/*
  Converts a character in the snake's body ("^<v>")
  to the matching character representing the snake's
  tail ("wasd").
*/
static char body_to_tail(char c) {
  switch (c){
    case '<': return 'a';
    case '>': return 'd';
    case 'v': return 's';
    case '^': return 'w';
  }
}

/*
  Converts a character in the snake's head ("WASD")
  to the matching character representing the snake's
  body ("^<v>").
*/
static char head_to_body(char c) {
  switch (c){
    case 'A': return '<';
    case 'D': return '>';
    case 'S': return 'v';
    case 'W': return '^';
  }
}

/*
  Returns cur_row + 1 if c is 'v' or 's' or 'S'.
  Returns cur_row - 1 if c is '^' or 'w' or 'W'.
  Returns cur_row otherwise.
*/
static unsigned int get_next_row(unsigned int cur_row, char c) {
  if (c == 'v' || c == 's' || c == 'S') {
    return cur_row + 1;
  }
  if (c == '^' || c == 'w' || c == 'W'){
    return cur_row - 1;
  }
  
  return cur_row;
}

/*
  Returns cur_col + 1 if c is '>' or 'd' or 'D'.
  Returns cur_col - 1 if c is '<' or 'a' or 'A'.
  Returns cur_col otherwise.
*/
static unsigned int get_next_col(unsigned int cur_col, char c) {
  if (c == '>' || c == 'd' || c == 'D') {
    return cur_col + 1;
  }
  if (c == '<' || c == 'a' || c == 'A'){
    return cur_col - 1;
  }
  
  return cur_col;
}

/*
  Task 4.2

  Helper function for update_state. Return the character in the cell the snake is moving into.

  This function should not modify anything.
*/
static char next_square(game_state_t* state, unsigned int snum) {
  snake_t curr_snake = state->snakes[snum];
  unsigned int head_c = curr_snake.head_col;
  unsigned int head_r = curr_snake.head_row;
  char snake_head = get_board_at(state, head_r, head_c);

  head_c = get_next_col(head_c, snake_head);
  head_r = get_next_row(head_r, snake_head);

  return get_board_at(state, head_r, head_c);
}

/*
  Task 4.3

  Helper function for update_state. Update the head...

  ...on the board: add a character where the snake is moving

  ...in the snake struct: update the row and col of the head

  Note that this function ignores food, walls, and snake bodies when moving the head.
*/
static void update_head(game_state_t* state, unsigned int snum) {
  snake_t curr_snake = state->snakes[snum];
  unsigned int head_c = curr_snake.head_col;
  unsigned int head_r = curr_snake.head_row;
  char snake_head = get_board_at(state, head_r, head_c);

  unsigned int update_c = get_next_col(head_c, snake_head);
  unsigned int update_r = get_next_row(head_r, snake_head);

  set_board_at(state, update_r, update_c, snake_head);
  set_board_at(state, head_r, head_c, head_to_body(snake_head));

  snake_t* curr = &state->snakes[snum];
  curr->head_col = update_c;
  curr->head_row = update_r;
}

/*
  Task 4.4

  Helper function for update_state. Update the tail...

  ...on the board: blank out the current tail, and change the new
  tail from a body character (^<v>) into a tail character (wasd)

  ...in the snake struct: update the row and col of the tail
*/
static void update_tail(game_state_t* state, unsigned int snum) {
  snake_t curr_snake = state->snakes[snum];
  unsigned int tail_c = curr_snake.tail_col;
  unsigned int tail_r = curr_snake.tail_row;
  char snake_tail = get_board_at(state, tail_r, tail_c);

  unsigned int update_c = get_next_col(tail_c, snake_tail);
  unsigned int update_r = get_next_row(tail_r, snake_tail);

  char new_tail = get_board_at(state, update_r, update_c);

  set_board_at(state, tail_r, tail_c, ' ');
  set_board_at(state, update_r, update_c, body_to_tail(new_tail));

  snake_t* curr = &state->snakes[snum];
  curr->tail_col = update_c;
  curr->tail_row = update_r;
}

/* Task 4.5 */
void update_state(game_state_t* state, int (*add_food)(game_state_t* state)) {
  unsigned int ns = state->num_snakes;

  for (unsigned int i = 0; i < ns; i++) {
    snake_t curr_snake = state->snakes[i];
    unsigned int head_c = curr_snake.head_col;
    unsigned int head_r = curr_snake.head_row;
    char snake_head = get_board_at(state, head_r, head_c);

    unsigned int update_c = get_next_col(head_c, snake_head);
    unsigned int update_r = get_next_row(head_r, snake_head);

    char new_head = get_board_at(state, update_r, update_c);

    if (new_head == '*'){
      update_head(state, i);
      add_food(state);
    }
    else if (new_head == '#' || is_snake(new_head)) {
      snake_t* curr = &state->snakes[i];
      curr->live = false;
      set_board_at(state, head_r, head_c, 'x');
    } else{
      update_head(state, i);
      update_tail(state, i);
    }
  }
  
  return;
}

/* Task 5 */
game_state_t* load_board(char* filename) {
  game_state_t *loaded_state;
  loaded_state = (game_state_t *) malloc(sizeof(game_state_t));
  if (!loaded_state) {return NULL;}

  FILE *fptr;
  fptr = fopen(filename, "r");
  if (!fptr) {return NULL;}

  char ch;
  unsigned int nr = 0;
  char all[999999] = "";

  do {
        ch = fgetc(fptr);
        strncat(all, &ch, 1);
        if (ch == '\n'){
          nr += 1;
        }

    } while (ch != EOF);
  

  loaded_state->num_rows = nr;
  loaded_state->board = malloc((nr + 1) * sizeof(char*));
  if (!loaded_state->board) {return NULL;}
  char *n = (char * ) 0;
  loaded_state->board[nr] = n;

  char* nl = strtok(all, "\n");
  int i = 0;
  while (nl && i < nr) {
    int row_len = strlen(nl);
    loaded_state->board[i] = malloc((row_len + 1) * sizeof(char));
    if (!loaded_state->board[i]) {return NULL;}
    strcpy(loaded_state->board[i], nl);

    nl = strtok(NULL, "\n");
    i += 1;
  }
  return loaded_state;
}

/*
  Task 6.1

  Helper function for initialize_snakes.
  Given a snake struct with the tail row and col filled in,
  trace through the board to find the head row and col, and
  fill in the head row and col in the struct.
*/
static void find_head(game_state_t* state, unsigned int snum) {
  snake_t curr_snake = state->snakes[snum];
  unsigned int col = curr_snake.tail_col;
  unsigned int row = curr_snake.tail_row;

  while (!is_head(get_board_at(state, row, col))){
    col = get_next_col(col, get_board_at(state, row, col));
    row = get_next_row(row, get_board_at(state, row, col));
  }

  snake_t* curr = &state->snakes[snum];
  curr->head_col = col;
  curr->head_row = row;

  return;
}

/* Task 6.2 */
game_state_t* initialize_snakes(game_state_t* state) {
  unsigned int nr = state->num_rows;
  state->snakes = (snake_t *) malloc(sizeof(snake_t*));
  //if (!state->snakes) {return NULL;}

  int count = 0;

  for (unsigned int i = 0; i < nr; i++) {
    unsigned int nc = strlen(state->board[i]);
    for (unsigned int j = 0; j < nc; j++) {
      if (is_tail(get_board_at(state, i, j))){
        snake_t *snek = (snake_t *) malloc(sizeof(snake_t));
        state->snakes[count] = *snek;
        state->snakes[count].tail_col = j;
        state->snakes[count].tail_row = i;
        state->snakes[count].live = true;
        count += 1;
        
      }
    }
  }
  

  state->num_snakes = count;
  for (unsigned int i = 0; i < count; i++){
    find_head(state, i);
  }
  
  return state;
}
