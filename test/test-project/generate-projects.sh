#https://github.com/cdsap/ProjectGenerator
#curl -L https://github.com/cdsap/ProjectGenerator/releases/download/v0.6.1/projectGenerator  --output projectGenerator
#chmod 0757 projectGenerator

module_shapes=("triangle" "rhombus" "flat" "rectangle" "middle_bottleneck" "inverse_triangle")
module_sizes=(20 20 10 20 20 20)
gradle_version=("gradle_9_4_1" "gradle_9_3_1" "gradle_9_2_1" "gradle_8_14_4" "gradle_8_13" "gradle_8_12_1")
layers=5 # DAG/layer's depth
language=both # kts & grovy

function generate_projects() {
  echo "-------- Generate Projects --------"
  for ((i = 0; i < ${#module_shapes[@]}; i++)) do
    project_name="${module_shapes[$i]}-${module_sizes[$i]}-${gradle_version[$i]}-shape-test-project"
    echo "Generate project $project_name"
    ./projectGenerator generate-project \
                        --project-name "$project_name" \
                        --shape "${module_shapes[$i]}" \
                        --modules "${module_sizes[$i]}" \
                        --gradle "${gradle_version[$i]}" \
                        --output-dir "./$project_name" \
                        --language "$language" \
                        --layers "$layers" > /dev/null && \
                        echo "Project was $project_name generated"
  done
}

function make_graphs() {
  echo "-------- Make Graphs --------"
  mkdir -p graphs
  for ((i = 0; i < ${#module_shapes[@]}; i++)) do
    project_name="${module_shapes[$i]}-${module_sizes[$i]}-${gradle_version[$i]}-shape-test-project"
    echo "Generating graph for project $project_name"
    dot -Tpng "./$project_name/project_groovy/graph.dot" -o \
      "./graphs/$project_name.graph.png"
  done
}

main() {
  arg=$1
  case $arg in
    "--generate-projects") generate_projects ;;
    "--generate-graphs") make_graphs ;;
    *) echo "Unknown command $arg"
  esac
}

main "$@"