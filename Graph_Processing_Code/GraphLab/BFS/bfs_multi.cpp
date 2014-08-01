/*
 *  BFS: some part of this code is originate from the GraphLab Toolkits, which is licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 */

#include <string>
#include <iostream>
#include <algorithm>
#include <vector>
#include <map>
#include <time.h>
#include <cstdlib>


#include <graphlab.hpp>
#include <graphlab/graph/distributed_graph.hpp>
#include <graphlab/graph/vertex_set.hpp>

struct vdata {
	int iter;
 // int label;
  vdata() :
      iter(std::numeric_limits<int>::max()) {
  }

  void save(graphlab::oarchive& oarc) const {
    oarc << iter;
  }
  void load(graphlab::iarchive& iarc) {
    iarc >> iter;
  }
};

typedef graphlab::distributed_graph<vdata, graphlab::empty> graph_type;

//Read a line from a file and create an edge, undirected
bool line_parser_u(graph_type& graph, const std::string& filename,
    const std::string& textline) {
  if (textline.length() == 0)
    return true;
  
	std::string rline = textline;
	int source;
	int target;
	int mark = 0;
	std::string tmp_s = "";
	mark = rline.find("\t");
	tmp_s=rline.substr(0,mark);
	rline.erase(0,mark+1);
	
	source = atoi(tmp_s.c_str());
	
	do
	{
		mark=0;
		tmp_s = "";
		mark = rline.find(",");
		if( -1 == mark )
		{
			tmp_s = rline.substr( 0, rline.length() );
			target= atoi(tmp_s.c_str());
			if (source != target)
				graph.add_edge(source, target);
			break;
		}
		tmp_s = rline.substr( 0, mark );
		rline.erase( 0, mark+1 );
		target=int(atoi(tmp_s.c_str()));
		if (source != target)
			graph.add_edge(source, target);
	}
	while(true);
	
  return true;
}

//Read a line from a file and create an edge, directed
bool line_parser_d(graph_type& graph, const std::string& filename,
    const std::string& textline) {
  if (textline.length() == 0)
    return true;
  
	std::string rline = textline;
	int source;
	int target;
	int mark = 0;
	std::string tmp_s = "";
	mark = rline.find("\t");
	tmp_s=rline.substr(0,mark);
	rline.erase(0,mark+1);
	
	source = atoi(tmp_s.c_str());
        mark = 0;
        mark = rline.find("@");
        rline.erase(0,mark+2);
	do
	{
		mark=0;
		tmp_s = "";
		mark = rline.find(",");
		if( -1 == mark )
		{
			tmp_s = rline.substr( 0, rline.length() );
			if(tmp_s.length()!=0 && tmp_s != "\n")
			{
				target= atoi(tmp_s.c_str());
				if (source != target)
					graph.add_edge(source, target);
			}
			break;
		}
		tmp_s = rline.substr( 0, mark );
		rline.erase( 0, mark+1 );
		target=int(atoi(tmp_s.c_str()));
		if (source != target)
			graph.add_edge(source, target);
	}
	while(true);
	
  return true;
}


//set label id at vertex id
void initialize_vertex(graph_type::vertex_type& v) {
  v.data().iter = 0;
}

struct min_message {
  int value;
  explicit min_message(int v) :
      value(v) {
  }
  min_message() :
      value(std::numeric_limits<int>::max()) {
  }
  min_message& operator+=(const min_message& other) {
    value = std::min<int>(value, other.value);
    return *this;
  }

  void save(graphlab::oarchive& oarc) const {
    oarc << value;
  }
  void load(graphlab::iarchive& iarc) {
    iarc >> value;
  }
};



class label_propergation: public graphlab::ivertex_program<graph_type, int, min_message>, public graphlab::IS_POD_TYPE {
private:
  int received_iter;
  bool perform_scatter;
public:
  label_propergation() {
    received_iter = std::numeric_limits<int>::max();
    perform_scatter = false;
  }
  void init(icontext_type& context, const vertex_type& vertex,
      const message_type& msg) {
    received_iter = msg.value;
  }
     
  //do not gather
  edge_dir_type gather_edges(icontext_type& context,
      const vertex_type& vertex) const {
    return graphlab::NO_EDGES;
  }
  int gather(icontext_type& context, const vertex_type& vertex,
      edge_type& edge) const {
    return 0;
  }

  //update label id. If updated, scatter messages
  void apply(icontext_type& context, vertex_type& vertex,
      const gather_type& total) {
    if (vertex.data().iter == 0){
	perform_scatter = true;	
    }	
    if (vertex.data().iter > received_iter) {
      perform_scatter = true;
      vertex.data().iter = received_iter += 1;
    }
  }

  edge_dir_type scatter_edges(icontext_type& context,
      const vertex_type& vertex) const {
    if (perform_scatter)
      return graphlab::OUT_EDGES;
    else
      return graphlab::NO_EDGES;
  }

  //If a neighbor vertex has a bigger label id, send a massage
  void scatter(icontext_type& context, const vertex_type& vertex,
      edge_type& edge) const {
	if (edge.target().id() != vertex.id()
        && edge.target().data().iter > vertex.data().iter) {
	context.signal(edge.target(), min_message(vertex.data().iter));
		
    }
  }
};

class graph_writer {
public:
  std::string save_vertex(graph_type::vertex_type v) {
    std::stringstream strm;
    strm << v.id() << "," << v.data().iter  << "\n";
    return strm.str();
  }
  std::string save_edge(graph_type::edge_type e) {
    return "";
  }
};


int globalroot = -2;
	bool is_rootvertex(const graph_type::vertex_type& vertex) {
		return vertex.id() == globalroot;
	}

int main(int argc, char** argv) {

  clock_t all_start, all_end;
  all_start = clock(); 
  std::cout << "Breadth First Search\n\n";

  graphlab::mpi_tools::init(argc, argv);
  graphlab::distributed_control dc;
  
  //parse options
  graphlab::command_line_options clopts("Breadth First Search.");
  std::string graph_dir;
  std::string saveprefix;
  //std::string format = "adj";
  std::string exec_type = "synchronous";
	int rootvertex = -2;
  std::string directivity;
  clopts.attach_option("graph", graph_dir,
                       "The graph file. This is not optional");
  clopts.add_positional("graph");
  clopts.attach_option("engine", exec_type,
                       "The engine type synchronous or asynchronous");
  //clopts.attach_option("format", format,
    //                   "The graph file format");
  clopts.attach_option("rootvertex", rootvertex,
			 "The start vertex of bfs");
  clopts.attach_option("direct", directivity,
                         "The directivity of original graph");
  clopts.attach_option("saveprefix", saveprefix,
                       "If set, will save the pairs of a vertex id and "
                       "a component id to a sequence of files with prefix "
                       "saveprefix");
  if (!clopts.parse(argc, argv)) {
    dc.cout() << "Error in parsing command line arguments." << std::endl;
    return EXIT_FAILURE;
  }
  if (graph_dir == "") {
    std::cout << "--graph is not optional\n";
    return EXIT_FAILURE;
  }

  if (directivity == "") {
    std::cout << "--graph directivity is not optional\n";
    return EXIT_FAILURE;
  }

	
	if (rootvertex == -2) {
		std::cout << "--rootvertex is not optional\n";
		return EXIT_FAILURE;
	}

  graph_type graph(dc, clopts);

  //load graph
  dc.cout() << "Loading graph.... "<< std::endl;
  //graph.load_format(graph_dir, format);
  if (directivity == "u")
  {  
    graph.load(graph_dir, line_parser_u);
  }
  if (directivity == "d")
  {
    graph.load(graph_dir, line_parser_d);
  }

  dc.cout() << "Load graph done"<< std::endl;
  graph.finalize();
  globalroot = rootvertex;
  graphlab::vertex_set rootset = graph.select(is_rootvertex);
	
  graph.transform_vertices(initialize_vertex, rootset);

  //running the engine
  clock_t start, end;
  graphlab::omni_engine<label_propergation> engine(dc, graph, exec_type, clopts);
  engine.signal_all();
  start=clock();
  engine.start();

  
  end=clock();
  dc.cout() << "graph calculation time is: (in sec) " << double((end - start))/CLOCKS_PER_SEC << "\n";
  
  //write results
  if (saveprefix.size() > 0) {
    graph.save(saveprefix, graph_writer(),
        false, //set to true if each output file is to be gzipped
        true, //whether vertices are saved
        false); //whether edges are saved
  }

  graphlab::mpi_tools::finalize();
  all_end = clock();
  dc.cout() << "Total time is: (in sec) " << double((all_end - all_start))/CLOCKS_PER_SEC << "\n";
  return EXIT_SUCCESS;
}

