# Traffic Management System using Graph Theory & Dijkstra’s Algorithm

This project was developed as part of our Data Structures and Algorithms (DSA) course.  
The aim of the project is to model an urban traffic network using graph theory and compute the most efficient routes using Dijkstra’s Algorithm.

Intersections are represented as nodes, roads as weighted edges, and travel time is used to determine the shortest and most optimal path between locations.

## Project Overview

Urban traffic congestion is a common issue in growing cities. This system simulates a city road network and helps identify the shortest path between two locations based on estimated travel time and traffic conditions.
The project also includes a GUI built using JavaFX to visually represent the city map and highlight the shortest path computed by the algorithm.

## Key Features

- Graph-based representation of traffic intersections and roads  
- Shortest path calculation using Dijkstra’s Algorithm  
- Priority Queue (Min-Heap) for efficient node selection  
- Interactive GUI for route visualization  
- Displays optimal path and total travel time  

## Technologies & Concepts Used

- Programming Language: Java  
- Core DSA Concepts: 
  - Graphs (Adjacency List)  
  - Dijkstra’s Algorithm  
  - Priority Queue (Min-Heap)  
- GUI: JavaFX  

## Data Structures Used

| Data Structure | Purpose |
|---------------|--------|
| Adjacency List | Efficient graph representation |
| Priority Queue | Selecting minimum distance node |
| HashMap | Storing distances and previous nodes |
| Arrays / Lists | Tracking visited nodes and paths |

## Algorithm Details

Dijkstra’s Algorithm is used to compute the shortest path between a source and destination node.

Time Complexity:  
O((V + E) log V)

Space Complexity: 
O(V + E)

Where:
- V = number of intersections  
- E = number of roads  
