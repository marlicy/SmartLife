package com.entity;

import java.util.*;

public class leetcode {
    public class TreeNode {
     int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int val) { this.val = val; }
     TreeNode(int val, TreeNode left, TreeNode right) {
          this.val = val;
         this.left = left;
         this.right = right;
    } }
   public boolean isSymmetric1(TreeNode root){
        return compare(root.left,root.right);
   }
   private boolean compare(TreeNode left,TreeNode right){
        if(left!=null&&right==null){
            return false;
        }
        if(left==null&&right!=null){
            return false;
        }
        if(left==null&&right==null){
            return true;

        }
        if(left.val!=right.val){
            return false;
        }
       boolean compareOutside = compare(left.left, right.right);
       boolean compareInside = compare(left.right, right.left);
       return compareInside && compareOutside;
    }
    /*class Node{
        public int val;
        public List<TreeNode> children;
        public Node(){}
        public Node(int _val){
            val=_val;
        }
        public Node(int _val,List<TreeNode> _children){
            val=_val;
            children=_children;
        }

    }*/



}
