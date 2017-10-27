

      subroutine expecttrue(x)
        logical x
        call assertTrue(x)
      end


      subroutine test_boolean_const()

        call expecttrue(.true.)
      end