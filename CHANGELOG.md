# Graphalytics Changelog

## v0.1: Alpha release

Changes since pre-alpha:

 - Added GraphX implementation of algorithms.
 - Added implementation of BFS using GraphLab Create
 - Reverted Giraph implementation to MapReduce-based implementation, available through Maven.
 - Refactored Giraph implementation:
   - Cleaned up codebase with respect to code duplication.
   - Removed MapReduce jobs for preprocessing.
 - Updated documentation of Giraph and MapReduce implementations.

