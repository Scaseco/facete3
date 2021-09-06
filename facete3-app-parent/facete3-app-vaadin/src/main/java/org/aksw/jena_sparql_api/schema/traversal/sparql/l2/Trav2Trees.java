package org.aksw.jena_sparql_api.schema.traversal.sparql.l2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;

public class Trav2Trees {

    public interface TreeNode2Visitor<X, A, B> {
        X visitA(A a);
        X visitB(B b);
    }

    public interface TreeNode<T> {
        /** The path leading to this node */
        Path<T> path();

        /** The parent node */
        TreeNode<T> parent();

        /** The value associated with this node */
        // S value();

        Stream<T> childKeys();
        TreeNode<T> child(T key); // Get existing child
        TreeNode<T> traverse(T key); // Get or create a new child
    }



    public interface TreeNode2<T, A extends TreeNode2<T, A, B>, B extends TreeNode2<T, A, B>>
        extends TreeNode<T>
    {
        default boolean isA() { return false; }
        default boolean isB() { return false; }

        default A asA() { return (A)this; }
        default B asB() { return (B)this; }

        <X> X accept(TreeNode2Visitor<X, A, B> visitor);
    }


    public interface TreeNode2Provider<T, A extends TreeNode2<T, A, B>, B extends TreeNode2<T, A, B>> {
        A mkRoot();

        B toB(A a, T segment);
        A toA(B b, T segment);

        A root();

        default TreeNode2<T, A, B> resolve(Path<T> path) {
            TreeNode<T> result = root();
            List<T> segments = path.getSegments();
            for (T segment: segments) {
                result = result.traverse(segment);
            }

            return (TreeNode2<T, A, B>)result;
        }
    }

    public static abstract class TreeNode2ProviderBase<T, A extends TreeNode2<T, A, B>, B extends TreeNode2<T, A, B>>
        implements TreeNode2Provider<T, A, B>
    {
        protected A root = null;

        @Override
        public A root() {
            if (root == null) {
                root = mkRoot();
            }

            return root;
        }
    }


    public static abstract class TreeNode2Base<T, A extends TreeNode2<T, A, B>, B extends TreeNode2<T, A, B>>
        implements TreeNode2<T, A, B>
    {
        protected Path<T> path;
        protected TreeNode2Provider<T, A, B> provider;

        public TreeNode2Base(Path<T> path, TreeNode2Provider<T, A, B> provider) {
            super();
            this.path = path;
            this.provider = provider;
        }

        protected abstract Map<T, ?> getChildMap();

        @Override
        public Path<T> path() {
            return path;
        }

        @Override
        public Stream<T> childKeys() {
            return getChildMap().keySet().stream();
        }
    }


    public static abstract class TreeNode2A<T, A extends TreeNode2<T, A, B>, B extends TreeNode2<T, A, B>>
        extends TreeNode2Base<T, A, B>
    {
        protected B parent;
        protected Map<T, B> childMap;

        public TreeNode2A(Path<T> path, TreeNode2Provider<T, A, B> provider, B parent) {
            super(path, provider);
            this.parent = parent;
            this.childMap = new LinkedHashMap<>();
        }

        @Override
        public boolean isA() {
            return true;
        }

        protected abstract B sendSelfToProvider(T key);

        @Override
        public B parent() {
            return parent;
        }

        @Override
        public B child(T key) {
            return childMap.get(key);
        }

        @Override
        protected Map<T, ?> getChildMap() {
            return childMap;
        }

        @Override
        public TreeNode<T> traverse(T key) {
            B b = childMap.computeIfAbsent(key, this::sendSelfToProvider);
            return b;
        }

        @Override
        public <X> X accept(TreeNode2Visitor<X, A, B> visitor) {
            A a = asA();
            X result = visitor.visitA(a);
            return result;
        }
    }


    public static abstract class TreeNode2B<T, A extends TreeNode2<T, A, B>, B extends TreeNode2<T, A, B>>
        extends TreeNode2Base<T, A, B>
    {
        protected A parent;
        protected Map<T, A> childMap;

        public TreeNode2B(Path<T> path, TreeNode2Provider<T, A, B> provider, A parent) {
            super(path, provider);
            this.parent = parent;
            this.childMap = new LinkedHashMap<>();
        }

        @Override
        public boolean isB() {
            return true;
        }

        protected abstract A sendSelfToProvider(T key);


        @Override
        public A parent() {
            return parent;
        }

        @Override
        public A child(T key) {
            return childMap.get(key);
        }

        @Override
        protected Map<T, ?> getChildMap() {
            return childMap;
        }

        @Override
        public TreeNode<T> traverse(T key) {
            A result = childMap.computeIfAbsent(key, this::sendSelfToProvider);
            return result;
        }

        @Override
        public <X> X accept(TreeNode2Visitor<X, A, B> visitor) {
            B a = asB();
            X result = visitor.visitB(a);
            return result;
        }
    }


}
