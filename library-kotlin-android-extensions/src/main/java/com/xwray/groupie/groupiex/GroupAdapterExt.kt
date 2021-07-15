package com.xwray.groupie.groupiex

import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

// TODO(zhuinden): move this into its own artifact later: `groupiex` (or rather, `groupie-ktx`)
operator fun GroupAdapter<out GroupieViewHolder>.plusAssign(element: Group) = this.add(element)
operator fun GroupAdapter<out GroupieViewHolder>.plusAssign(groups: List<Group>) = this.addAll(groups)
operator fun GroupAdapter<out GroupieViewHolder>.minusAssign(element: Group) = this.remove(element)
operator fun GroupAdapter<out GroupieViewHolder>.minusAssign(groups: List<Group>)  = this.removeAll(groups)