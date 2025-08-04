package com.voiz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Menus;

@Repository
public interface MenusRepository extends JpaRepository<Menus, Integer> {

}
