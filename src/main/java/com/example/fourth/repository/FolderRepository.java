package com.example.fourth.repository;

import com.example.fourth.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByNameAndUser_Id(String name, int userId);
    List<Folder> findByUser_Id(int userId);

}