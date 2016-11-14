package br.pucrio.opus.smells.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import br.pucrio.opus.smells.ast.visitors.MethodCollector;

public class Type extends Resource {

	private List<Method> methods;
	
	private transient Set<Type> children;
	
	public TypeDeclaration getNodeAsTypeDeclaration() {
		return (TypeDeclaration)getNode();
	}
	
	public String getSuperclassFQN() {
		ITypeBinding binding = this.getNodeAsTypeDeclaration().resolveBinding();
		if (binding != null) {
			ITypeBinding superclass = binding.getSuperclass();
			if (superclass != null) {
				return superclass.getQualifiedName();
			}
		}
		return null;
	}
	
	public ITypeBinding resolveSuperclassBinding() {
		ITypeBinding binding = this.getNodeAsTypeDeclaration().resolveBinding();
		if (binding != null) {
			ITypeBinding superclass = binding.getSuperclass();
			return superclass;
		}
		return null;
	}
	
	public Type(SourceFile sourceFile, TypeDeclaration typeDeclaration) {
		super(sourceFile, typeDeclaration);
		
		this.children = new HashSet<>();
		
		IBinding binding = typeDeclaration.resolveBinding();
		if (binding != null) {
			String fqn = typeDeclaration.resolveBinding().getQualifiedName();
			setFullyQualifiedName(fqn);
		}
		this.searchForMethods();
		
		//register itself in the ParenthoodRegistry 
		ParenthoodRegistry.getInstance().registerChild(this);
	}
	
	private void searchForMethods() {
		this.methods = new ArrayList<>();
		MethodCollector visitor = new MethodCollector();
		this.getNode().accept(visitor);
		List<MethodDeclaration> methodsDeclarations = visitor.getNodesCollected();
		for (MethodDeclaration methodDeclaration : methodsDeclarations) {
			Method method = new Method(getSourceFile(), methodDeclaration);
			this.methods.add(method);
		}
	}
	
	public Method findMethodByName(String name) {
		for (Method method : this.methods) {
			String toBeFound = this.getFullyQualifiedName() + "." + name;
			if (method.getFullyQualifiedName().equals(toBeFound)) {
				return method;
			}
		}
		return null;
	}
	
	@Override
	public boolean isSmelly() {
		if (super.isSmelly()) {
			return true;
		}
		
		for (Method method : this.methods) {
			if (method.isSmelly()) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<Method> getMethods() {
		return methods;
	}
	
	public Set<Type> getChildren() {
		return children;
	}
	
	@Override
	public String toString() {
		return "Type [fqn=" + getFullyQualifiedName() + "]";
	}
	
}
