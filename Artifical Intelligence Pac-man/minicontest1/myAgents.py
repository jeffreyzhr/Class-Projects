# myAgents.py
# ---------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
#
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).

import operator
from game import Agent
from pacman import GameState
from searchProblems import PositionSearchProblem, manhattanHeuristic

import util
import time
import search

"""
IMPORTANT
`agent` defines which agent you will use. By default, it is set to ClosestDotAgent,
but when you're ready to test your own agent, replace it with MyAgent
"""
def createAgents(num_pacmen, agent='col_agent'):
    return_arr = []
    for i in range(num_pacmen):
        if i == 0:
            return_arr.append(eval('col_agent')(index= i))
        elif i == 1:
            return_arr.append(eval('col_agent')(index= i))
        elif i == 2:
            return_arr.append(eval('col_agent')(index= i))
        elif i == 3:
            return_arr.append(eval('col_agent')(index= i))
        else:
            return_arr.append(eval(agent)(index= i))
    
    return return_arr


class col_agent(Agent):
    
    def initialize(self):
        self.return_array = []
    
    def getAction(self, state):
        num_agents = state.getNumPacmanAgents()
        board_width = state.getWidth()
        cw = board_width//num_agents
        
        left_bound = self.index*cw
        if self.index == num_agents - 1:
            right_bound = board_width
        else:
            right_bound = (self.index + 1)* cw
                
        problem = col_problem(state, self.index, left_bound, right_bound)
        if not self.return_array:
            #self.return_array = search.astar(problem, dist_heuristic)
            self.return_array = search.bfs(problem)
        return self.return_array.pop(0)

    
class col_problem(PositionSearchProblem):
    def __init__(self, gameState, agentIndex, left_b, right_b):
        self.food = gameState.getFood()
        self.walls = gameState.getWalls()
        self.startState = gameState.getPacmanPosition(agentIndex)
        self.costFn = lambda x: 1
        self._visited, self._visitedlist, self._expanded = {}, [], 0 # DO NOT CHANGE

        self.food_loc = []
        for i in range(left_b, right_b):
            for j in range(gameState.getHeight()):
                if self.food[i][j]:
                    self.food_loc.append((i,j))
    
    def isGoalState(self, state):
        x, y = state
        
        if self.food_loc:
            return state in self.food_loc
        else:
            return self.food[x][y]




    
    
    