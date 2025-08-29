#!/usr/bin/env python3

import os
import re
from pathlib import Path

def extract_package_name(java_content):
    """Extract package name from Java file content"""
    package_match = re.search(r'package\s+([^;]+);', java_content)
    return package_match.group(1) if package_match else ""

def extract_class_info(java_content):
    """Extract class/interface name and type from Java file content"""
    # Remove comments and strings to avoid false matches
    clean_content = re.sub(r'//.*?$', '', java_content, flags=re.MULTILINE)
    clean_content = re.sub(r'/\*.*?\*/', '', clean_content, flags=re.DOTALL)
    clean_content = re.sub(r'"[^"]*"', '""', clean_content)
    
    # Look for class or interface declaration
    class_match = re.search(r'(public\s+)?(abstract\s+)?(class|interface|enum)\s+(\w+)', clean_content)
    if class_match:
        class_type = class_match.group(3)  # class, interface, or enum
        class_name = class_match.group(4)
        return class_name, class_type
    return None, None

def extract_fields(java_content):
    """Extract field declarations from Java file content"""
    # Remove comments, strings, and method bodies
    clean_content = re.sub(r'//.*?$', '', java_content, flags=re.MULTILINE)
    clean_content = re.sub(r'/\*.*?\*/', '', clean_content, flags=re.DOTALL)
    clean_content = re.sub(r'"[^"]*"', '""', clean_content)
    
    fields = []
    
    # Pattern to match field declarations (simplified)
    # Matches: [modifiers] type fieldName [= value];
    field_pattern = r'((?:private|public|protected|static|final|volatile)\s+)*(\w+(?:<[^>]*>)?(?:\[\])*)\s+(\w+)(?:\s*=\s*[^;]+)?;'
    
    # Split by class/method boundaries to avoid capturing method parameters
    lines = clean_content.split('\n')
    in_method = False
    brace_count = 0
    
    for line in lines:
        line = line.strip()
        if not line or line.startswith('import') or line.startswith('package'):
            continue
            
        # Track if we're inside a method
        if re.match(r'.*\w+\s*\([^)]*\)\s*\{?', line) and not re.match(r'.*(class|interface|enum)\s+\w+', line):
            in_method = True
            brace_count = line.count('{') - line.count('}')
        elif in_method:
            brace_count += line.count('{') - line.count('}')
            if brace_count <= 0:
                in_method = False
                brace_count = 0
        
        # Only look for fields outside of methods
        if not in_method and ';' in line:
            field_match = re.match(field_pattern, line)
            if field_match:
                modifiers = field_match.group(1) or ""
                field_type = field_match.group(2)
                field_name = field_match.group(3)
                
                # Convert to PlantUML visibility
                if 'private' in modifiers:
                    visibility = '-'
                elif 'protected' in modifiers:
                    visibility = '#'
                elif 'public' in modifiers:
                    visibility = '+'
                else:
                    visibility = '+'  # package-private
                
                # Add static indicator
                static_marker = ' {static}' if 'static' in modifiers else ''
                
                fields.append(f"  {visibility} {field_name}: {field_type} {static_marker}")
    
    return fields

def extract_methods(java_content):
    """Extract method declarations from Java file content"""
    # Remove comments and strings
    clean_content = re.sub(r'//.*?$', '', java_content, flags=re.MULTILINE)
    clean_content = re.sub(r'/\*.*?\*/', '', clean_content, flags=re.DOTALL)
    clean_content = re.sub(r'"[^"]*"', '""', clean_content)
    
    methods = []
    
    # Pattern to match method declarations
    method_pattern = r'((?:private|public|protected|static|abstract|final|synchronized)\s+)*(\w+(?:<[^>]*>)?(?:\[\])*|\w+)\s+(\w+)\s*\(([^)]*)\)\s*(?:throws\s+[^{]+)?\s*[{;]'
    
    method_matches = re.finditer(method_pattern, clean_content)
    
    for match in method_matches:
        modifiers = match.group(1) or ""
        return_type = match.group(2)
        method_name = match.group(3)
        parameters = match.group(4) or ""

        # Skip constructors (return type same as method name pattern doesn't apply well here)
        # This is a simplified check
        
        if 'new' in return_type:
            continue

        # Convert to PlantUML visibility
        if 'private' in modifiers:
            visibility = '-'
        elif 'protected' in modifiers:
            visibility = '#'
        elif 'public' in modifiers:
            visibility = '+'
        else:
            visibility = '+'  # package-private
        
        # Add static/abstract indicators
        static_marker = ' {static}' if 'static' in modifiers else ''
        abstract_marker = ' {abstract}' if 'abstract' in modifiers else ''
        
        # Clean up parameters
        param_list = []
        if parameters.strip():
            for param in parameters.split(','):
                param = param.strip()
                if param:
                    # Extract parameter type and name
                    param_parts = param.split()
                    if len(param_parts) >= 2:
                        param_type = param_parts[-2] if len(param_parts) > 2 else param_parts[0]
                        param_name = param_parts[-1]
                        param_list.append(f"{param_name}: {param_type}")
        
        param_str = ", ".join(param_list)

        # constructor
        if 'public' in return_type or 'private' in return_type:
            return_type = ''

        methods.append(f"  {visibility} {method_name}({param_str}): {return_type}{static_marker}{abstract_marker}")
    
    return methods

def process_java_file(file_path):
    """Process a single Java file and return PlantUML class definition"""
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        package = extract_package_name(content)
        class_name, class_type = extract_class_info(content)
        
        if not class_name:
            return None
        
        fields = extract_fields(content)
        methods = extract_methods(content)
        
        # Generate PlantUML class definition
        uml_lines = []
        
        # Class declaration
        if class_type == 'interface':
            uml_lines.append(f"interface {class_name} {{")
        elif class_type == 'enum':
            uml_lines.append(f"enum {class_name} {{")
        else:  # class
            uml_lines.append(f"class {class_name} {{")
        
        # Add fields
        if fields:
            uml_lines.extend(fields)
            if methods:  # Add separator if both fields and methods exist
                uml_lines.append("  --")
        
        # Add methods
        if methods:
            uml_lines.extend(methods)
        
        uml_lines.append("}")
        uml_lines.append("")  # Empty line for readability
        
        return '\n'.join(uml_lines)
        
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return None

def scan_java_files(directory):
    """Recursively scan directory for .java files"""
    java_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

def generate_plantuml_diagram(directory, output_file):
    """Generate PlantUML diagram from all Java files in directory"""
    print(f"Scanning directory: {directory}")
    java_files = scan_java_files(directory)
    
    if not java_files:
        print("No Java files found!")
        return
    
    print(f"Found {len(java_files)} Java files")
    
    # Start PlantUML diagram
    uml_content = ["@startuml"]
    uml_content.append("!theme plain")
    uml_content.append("skinparam classAttributeIconSize 0")
    uml_content.append("")
    
    processed_count = 0
    
    for java_file in java_files:
        print(f"Processing: {java_file}")
        class_uml = process_java_file(java_file)
        if class_uml:
            uml_content.append(class_uml)
            processed_count += 1
    
    uml_content.append("@enduml")
    
    # Write to output file
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write('\n'.join(uml_content))
        print(f"Successfully generated {output_file} with {processed_count} classes")
    except Exception as e:
        print(f"Error writing output file: {e}")

def main():
    # Get current directory or specify your Java project directory
    current_dir = "."  # Change this to your Java project path
    output_file = "uml.puml"
    
    print("Java to PlantUML Converter")
    print("=" * 30)
    
    # Allow user to specify directory
    user_dir = input(f"Enter directory path (press Enter for current directory '{current_dir}'): ").strip()
    if user_dir:
        current_dir = user_dir
    
    # Check if directory exists
    if not os.path.isdir(current_dir):
        print(f"Directory '{current_dir}' does not exist!")
        return
    
    generate_plantuml_diagram(current_dir, output_file)
    print(f"\nDone! Check '{output_file}' for the generated PlantUML diagram.")
    print("You can view it using PlantUML tools or VS Code with PlantUML extension.")

if __name__ == "__main__":
    main()
